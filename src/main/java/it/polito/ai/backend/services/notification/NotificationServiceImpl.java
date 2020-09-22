package it.polito.ai.backend.services.notification;

import it.polito.ai.backend.controllers.ModelHelper;
import it.polito.ai.backend.dtos.StudentDTO;
import it.polito.ai.backend.dtos.TeamDTO;
import it.polito.ai.backend.dtos.TokenDTO;
import it.polito.ai.backend.entities.*;
import it.polito.ai.backend.repositories.CourseRepository;
import it.polito.ai.backend.repositories.StudentRepository;
import it.polito.ai.backend.repositories.TeamRepository;
import it.polito.ai.backend.repositories.TokenRepository;
import it.polito.ai.backend.services.Utils;
import it.polito.ai.backend.services.team.StudentNotEnrolledException;
import it.polito.ai.backend.services.team.StudentNotFoundException;
import it.polito.ai.backend.services.team.TeamNotFoundException;
import it.polito.ai.backend.services.team.TeamService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@EnableAsync
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    JavaMailSender emailSender;
    @Autowired
    TokenRepository tokenRepository;
    @Autowired
    TeamService teamService;
    @Autowired
    ModelMapper modelMapper;
    @Autowired
    StudentRepository studentRepository;
    @Autowired
    TeamRepository teamRepository;

    @Async
    @Override
    public void sendMessage(String address, String subject, String body ,String sender) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(sender);
        message.setTo(address);
        message.setSubject(subject);
        message.setText(body);
        emailSender.send(message);
    }

    @Override
    @PreAuthorize("hasRole('STUDENT') and @securityServiceImpl.hasToken(#tokenId)")
    public String confirm(String tokenId) {

        Optional<Token> tokenOptional = tokenRepository.findById(tokenId);

        if (!tokenOptional.isPresent()) {
            throw new TokenNotFoundException(tokenId);
        }

        long nowLong = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        Timestamp now = new Timestamp(nowLong);
        boolean isTokenExpired = tokenRepository.findAllByExpiryDateBefore(now).stream().anyMatch(t -> t.getId().equals(tokenId));

        if (isTokenExpired) {
            throw new TokenExpiredException(tokenId);
        }
        /*the student can accept only one time */
        if(!tokenOptional.get().getStatus().equals(TokenStatus.UNDEFINED))
            throw new TokenExpiredException(tokenId);

        String studentId = tokenOptional.get().getStudentId();
        Optional<Student> student = studentRepository.findById(studentId);
        if(!student.isPresent())
            throw new StudentNotFoundException(studentId);

        Long teamId = tokenOptional.get().getTeamId();
        Optional<Team> team = teamRepository.findById(teamId);
        if(!team.isPresent())
            throw new TeamNotFoundException(teamId.toString());

        if(student.get().getCourses().stream().noneMatch(c -> c.getName().equals(team.get().getCourse().getName())))
            throw new StudentNotEnrolledException(studentId);

        if(team.get().getMembers().stream().noneMatch(m -> m.getId().equals(studentId)))
            throw new StudentNotFoundException(studentId);


        List<Token> teamTokens = tokenRepository.findAllByTeamId(teamId);
        //disable the request
        if(teamTokens.stream().anyMatch(token -> token.getStatus().equals(TokenStatus.REJECT)))
            //return false;
            return "Thanks for your choice. You can see the status of the team in your private area.";

        //remove the current token from the list to check the other tokens
        teamTokens.remove(tokenOptional.get());

        boolean isLastToken = teamTokens.stream().allMatch(token -> token.getStatus().equals(TokenStatus.ACCEPT));
        Course courseTeam =  team.get().getCourse();
        List<Team> teamsStudent = student.get().getTeams();
        for (Team teamStudent:teamsStudent) {
            if(teamStudent.getCourse().equals(courseTeam) && teamStudent.getStatus().equals(TeamStatus.ACTIVE)){
                // the student can not accept a propose for a team in the same course if he has a confirmed team
                tokenRepository.save(tokenOptional.get());
                //return false;
                return "Thanks for your choice. You can see the status of the team in your private area.";

            }

        }

        if (isLastToken) {
            teamService.confirmTeam(teamId);
            tokenOptional.get().setStatus(TokenStatus.ACCEPT);
            tokenRepository.save(tokenOptional.get());
            //return true;
            return "Thanks for your choice. You can see the status of the team in your private area.";
        }

        tokenOptional.get().setStatus(TokenStatus.ACCEPT);
        tokenRepository.save(tokenOptional.get());
        //return false;
        return "Thanks for your choice. You can see the status of the team in your private area.";
    }

    @Override
    @PreAuthorize("hasRole('STUDENT') and @securityServiceImpl.hasToken(#tokenId)")
    public String reject(String tokenId) {
        Optional<Token> tokenOptional = tokenRepository.findById(tokenId);

        if (!tokenOptional.isPresent()) {
            throw new TokenNotFoundException(tokenId);
        }

        long nowLong = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        Timestamp now = new Timestamp(nowLong);
        boolean isTokenExpired = tokenRepository.findAllByExpiryDateBefore(now).stream().anyMatch(t -> t.getId().equals(tokenId));

        if (isTokenExpired) {
            throw new TokenExpiredException(tokenId);
        }
        //the student can reject only one time
        if(!tokenOptional.get().getStatus().equals(TokenStatus.UNDEFINED))
            throw new TokenExpiredException(tokenId);

        Long teamId = tokenOptional.get().getTeamId();
        List<Token> teamTokens = tokenRepository.findAllByTeamId(teamId);
        if(teamTokens.stream().anyMatch(token -> token.getStatus().equals(TokenStatus.REJECT)))
            // request disabled
            //return false;
            return "Thanks for your choice. You can see the status of the team in your private area.";

        tokenOptional.get().setStatus(TokenStatus.REJECT);
        tokenRepository.save(tokenOptional.get());
        //return true;
        return "Thanks for your choice. You can see the status of the team in your private area.";
    }

    @Override
    public boolean addToken(TokenDTO token) {
        if (!tokenRepository.existsById(token.getId())) {
            Token t = modelMapper.map(token, Token.class);
            tokenRepository.save(t);
            return true;
        }
        return false;
    }

    @Override
    @PreAuthorize("hasRole('STUDENT') and @securityServiceImpl.isPartOf(#teamId)")
    public List<TokenDTO> getTokenTeam(Long teamId) {
        if(!teamRepository.findById(teamId).isPresent())
            throw new TeamNotFoundException("Not exist team: "+teamId.toString());


        return tokenRepository.findAllByTeamId(teamId)
                .stream()
                .map(t-> modelMapper.map(t,TokenDTO.class))
                .collect(Collectors.toList());

    }

    @Override
    @PreAuthorize("hasRole('STUDENT') and @securityServiceImpl.isAuthorized(#proponent)")
    public void notifyTeam(TeamDTO teamDTO, List<String> memberIds, Timestamp timeout, String proponent) {

        List<StudentDTO> members = teamService.getMembers(teamDTO.getId());
        if (!(members.size()-1 == memberIds.size())){
            throw new StudentNotFoundException(members.toString());
        }

        memberIds.forEach(id -> {
            String tokenId = UUID.randomUUID().toString();
            TokenDTO tokenDTO = new TokenDTO(tokenId, teamDTO.getId(), id, TokenStatus.UNDEFINED, timeout);
            if (addToken(tokenDTO)) {
                TokenDTO enrichedConfirmToken = ModelHelper.enrich(tokenDTO, "confirm");
                TokenDTO enrichedRejectToken = ModelHelper.enrich(tokenDTO, "reject");

                StudentDTO currentStudent = teamService.getStudent(id).orElseThrow(() -> new StudentNotFoundException(id));
                String address = currentStudent.getEmail();
                String subject = "Request for team creation";
                StringBuilder body = new StringBuilder(String.format("Hi %s %s,\n", currentStudent.getFirstName(), currentStudent.getLastName()));
                body.append(String.format("you have been added to a team (%s).\n\n", teamDTO.getName()));
                body.append("Group attendees are:\n");
                for (StudentDTO s : members) {
                    body.append(String.format("- %s, %s %s\n", s.getId(), s.getFirstName(), s.getLastName()));
                }
                body.append(String.format("\nIf you want to confirm please click the following link:\n%s\n\n", enrichedConfirmToken.getLink("confirm").get().getHref()));
                body.append(String.format("Otherwise, if you want to refuse this invitation, please click the following link:\n%s\n\n", enrichedRejectToken.getLink("reject").get().getHref()));
                body.append("\nRegards.");

                sendMessage(address, subject, body.toString(),"asant.lab3@gmail.com");
            } else {
                throw new DuplicateTokenException(tokenId);
            }
        });
    }
}
