package it.polito.ai.backend.services;

import it.polito.ai.backend.controllers.ModelHelper;
import it.polito.ai.backend.dtos.StudentDTO;
import it.polito.ai.backend.dtos.TeamDTO;
import it.polito.ai.backend.dtos.TokenDTO;
import it.polito.ai.backend.entities.Token;
import it.polito.ai.backend.repositories.TokenRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

    @Async
    @Override
    public void sendMessage(String address, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(address);
        message.setSubject(subject);
        message.setText(body);
        emailSender.send(message);
    }

    @Override
    public boolean confirm(String tokenId) {
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

        Long teamId = tokenOptional.get().getTeamId();
        List<Token> teamTokens = tokenRepository.findAllByTeamId(teamId);
        boolean isLastToken = teamTokens.size() == 1;

        if (isLastToken) {
            teamService.confirmTeam(teamId);
            tokenRepository.delete(tokenOptional.get());
            return true;
        }

        tokenRepository.delete(tokenOptional.get());
        return false;
    }

    @Override
    public boolean reject(String tokenId) {
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

        Long teamId = tokenOptional.get().getTeamId();
        List<Token> teamTokens = tokenRepository.findAllByTeamId(teamId);

        tokenRepository.deleteAll(teamTokens);
        teamService.evictTeam(teamId);
        return true;
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
    public void notifyTeam(TeamDTO teamDTO, List<String> memberIds) {

        long nowPlusOneHourLong = LocalDateTime.now().plusHours(1).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        Timestamp nowPlusOneHour = new Timestamp(nowPlusOneHourLong);
        List<StudentDTO> members = teamService.getMembers(teamDTO.getId());
        if (!(members.size() == memberIds.size())){
            throw new StudentNotFoundException(members.toString());
        }
        memberIds.forEach(id -> {
            String tokenId = UUID.randomUUID().toString();
            TokenDTO tokenDTO = new TokenDTO(tokenId, teamDTO.getId(), nowPlusOneHour);
            if (addToken(tokenDTO)) {
                TokenDTO enrichedConfirmToken = ModelHelper.enrich(tokenDTO, "confirm");
                TokenDTO enrichedRejectToken = ModelHelper.enrich(tokenDTO, "reject");
                String address = "s"+id+"@studenti.polito.it";
                String myAddress = "t.santoro1993@gmail.com";
                StudentDTO currentStudent = teamService.getStudent(id).orElseThrow(() -> new StudentNotFoundException(id));
                String subject = "Request for team creation";
                StringBuilder body = new StringBuilder(String.format("Hi %s %s,\n", currentStudent.getName(), currentStudent.getFirstName()));
                body.append(String.format("you have been added to a team (%s).\n\n", teamDTO.getName()));
                body.append("Group attendees are:\n");
                for (StudentDTO s : members) {
                    body.append(String.format("- %s, %s %s\n", s.getId(), s.getName(), s.getFirstName()));
                }
                body.append(String.format("\nIf you want to confirm please click the following link:\n%s\n\n", enrichedConfirmToken.getLink("confirm").get().getHref()));
                body.append(String.format("Otherwise, if you want to refuse this invitation, please click the following link:\n%s\n\n", enrichedRejectToken.getLink("reject").get().getHref()));
                body.append("\nRegards.");
                sendMessage(myAddress, subject, body.toString());
            } else {
                throw new DuplicateTokenException(tokenId);
            }
        });
    }
}
