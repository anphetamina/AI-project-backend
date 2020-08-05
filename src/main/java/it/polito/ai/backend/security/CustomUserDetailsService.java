package it.polito.ai.backend.security;

import it.polito.ai.backend.controllers.ModelHelper;
import it.polito.ai.backend.dtos.*;
import it.polito.ai.backend.entities.ConfirmationToken;
import it.polito.ai.backend.entities.User;
import it.polito.ai.backend.repositories.UserRepository;
import it.polito.ai.backend.repositories.ConfirmationTokenRepository;
import it.polito.ai.backend.services.Utils;
import it.polito.ai.backend.services.team.TeamServiceImpl;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private UserRepository userRepository;
    @Autowired
    JavaMailSender emailSender;
    @Autowired
    ConfirmationTokenRepository confirmationTokenRepository;
    @Autowired
    ModelMapper modelMapper;
    @Autowired
    TeamServiceImpl teamService;

    private final BCryptPasswordEncoder bCryptPasswordEncoder;



    @Override
    public UserDetails loadUserByUsername(String userIdOrEmail) throws UsernameNotFoundException {

        Optional<User> user = userRepository.findById(userIdOrEmail);

        if(!user.isPresent())
            throw  new UsernameNotFoundException("Username: "+userIdOrEmail+" not found");
        if( !user.get().isEnable())
            throw  new UsernameNotFoundException("Username: "+userIdOrEmail+" not found");
        return user.get();

    }



    void sendConfirmationMail(String userMail, ConfirmationTokenDTO token) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        ConfirmationTokenDTO enrichToken = ModelHelper.enrich(token);
        mailMessage.setTo(userMail);
        //todo email segreteria
        mailMessage.setFrom("ralucagabriela.tabacaru@studenti.polito.it");
        mailMessage.setSubject("Mail Confirmation Link!");
        mailMessage.setText(
                "Thank you for registering. Please click on the below link to activate your account. " + enrichToken.getLink("confirmation").get().getHref() );
        emailSender.send(mailMessage);
    }


    public boolean addConfirmationToken(ConfirmationTokenDTO confirmationTokenDTO) {
        if (!confirmationTokenRepository.existsById(confirmationTokenDTO.getId())) {
            ConfirmationToken confirmationToken = modelMapper.map(confirmationTokenDTO, ConfirmationToken.class);
            confirmationTokenRepository.save(confirmationToken);
            return true;
        }
        return false;
    }

    public void signUpUser(UserInformationRequest data, Byte[] image)  {
        if(userRepository.findByEmail(data.getEmail()).isPresent()
                || (!data.getEmail().contains("@polito.it") && !data.getEmail().contains("@studenti.polito.it")))
            throw new SecurityServiceException("Username  not valid");
        if(!data.getPassword().equals(data.getRepeatPassword()))
            throw new SecurityServiceException("Password not valid");
        String encryptedPassword = bCryptPasswordEncoder.encode(data.getPassword());
        User user = new User();
        user.setId(data.getId());
        user.setEmail(data.getEmail());
        user.setPassword(encryptedPassword);
        if(data.getEmail().contains("@polito.it")) {
            user.setRoles(Arrays.asList("ROLE_TEACHER"));
            TeacherDTO teacherDTO = new TeacherDTO(data.getId(),data.getLastName(),
                    data.getFirstName(),data.getEmail(),image);
            teamService.addTeacher(teacherDTO);
        }

        else {
            user.setRoles(Arrays.asList("ROLE_STUDENT"));
            StudentDTO studentDTO = new StudentDTO(data.getId(),data.getLastName(),
                    data.getFirstName(),data.getEmail(),image);
            teamService.addStudent(studentDTO);
        }
        userRepository.save(user);

        ConfirmationTokenDTO confirmationTokenDTO = new ConfirmationTokenDTO();
        confirmationTokenDTO.setUsername(user.getId());
        Timestamp expiredDate = new Timestamp(Utils.getNow().getTime() + (24*3600000));
        System.out.println(expiredDate);
        confirmationTokenDTO.setExpiryDate(expiredDate);
        confirmationTokenDTO.setId(UUID.randomUUID().toString());
        if(!addConfirmationToken(confirmationTokenDTO))
            throw  new DuplicateConfirmationToken("Duplicate confirmation token");

        sendConfirmationMail(user.getEmail(), confirmationTokenDTO);
    }

    public boolean confirmUser(String  confirmationTokenId) {
        Optional<ConfirmationToken> tokenOptional = confirmationTokenRepository.findById(confirmationTokenId);
        if (!tokenOptional.isPresent()) {
            throw new SecurityServiceException("Expired session");
        }
        if(tokenOptional.get().getExpiryDate().before(Utils.getNow()))
            throw new SecurityServiceException("Expired session");

        Optional<User> user = userRepository.findById(tokenOptional.get().getUsername());
        if(!user.isPresent())
            throw new UsernameNotFoundException("User not found");
        if(user.get().isEnable())
            return false;
        user.get().setEnable(true);
        userRepository.save(user.get());
        confirmationTokenRepository.delete(tokenOptional.get());
        return true;
    }

    public String getId(String username){
       return  userRepository.findByEmail(username).orElseThrow( () -> new
                    UsernameNotFoundException("Username " + username + "not found")).getId();

    }

    public List<String> getRoles(String id){
        return userRepository.findById(id).orElseThrow( () -> new
                UsernameNotFoundException("Username " + id + "not found")).getRoles();
    }


    public void deleteUser(String userId){
        Optional<User> user = userRepository.findById(userId);
        if(!user.isPresent())
            throw new UsernameNotFoundException("Invalid user id");
        if(user.get().getAuthorities().size()>0){
            for(GrantedAuthority role : user.get().getAuthorities()){
                user.get().getAuthorities().remove(role);
            }
        }
        userRepository.delete(user.get());

    }
}
