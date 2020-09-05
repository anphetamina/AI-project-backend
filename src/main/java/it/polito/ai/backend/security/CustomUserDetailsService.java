package it.polito.ai.backend.security;

import it.polito.ai.backend.controllers.ModelHelper;
import it.polito.ai.backend.dtos.*;
import it.polito.ai.backend.entities.ConfirmationToken;
import it.polito.ai.backend.entities.Student;
import it.polito.ai.backend.entities.Teacher;
import it.polito.ai.backend.entities.User;
import it.polito.ai.backend.repositories.StudentRepository;
import it.polito.ai.backend.repositories.TeacherRepository;
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
    @Autowired
    TeacherRepository teacherRepository;
    @Autowired
    StudentRepository studentRepository;

    private final BCryptPasswordEncoder bCryptPasswordEncoder;



    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {

        Optional<User> user = userRepository.findById(userId);

        if(!user.isPresent())
            throw  new UsernameNotFoundException("Username: "+userId+" not found");
        if( !user.get().isEnabled())
            throw  new UsernameNotFoundException("Username: "+userId+" not found");
        return user.get();

    }



    void sendConfirmationMail(String userMail, ConfirmationTokenDTO token) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        ConfirmationTokenDTO enrichToken = ModelHelper.enrich(token);
        mailMessage.setTo(userMail);
        mailMessage.setFrom("asant.lab3@gmail.com");
        mailMessage.setSubject("Mail Confirmation Link!");
        mailMessage.setText("Thank you for registering. Please click on the below link to activate your account.\n "
                + enrichToken.getLink("confirmation").get().getHref() );
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

    public void signUpUser(UserInformationRequest data, byte[] image)  {
        /*check if there is an other user with the same email and if email is of polito.it*/
        if(userRepository.findByEmail(data.getEmail()).isPresent()
                || (!data.getEmail().contains("@polito.it") && !data.getEmail().contains("@studenti.polito.it")))
            throw new SecurityServiceException("Email not valid");
       /*check if passwords are the same */
        if(!data.getPassword().equals(data.getRepeatPassword()))
            throw new SecurityServiceException("Password not valid");
        String encryptedPassword = bCryptPasswordEncoder.encode(data.getPassword());


        User user = new User();
        user.setId(data.getId());
        user.setEmail(data.getEmail());
        user.setPassword(encryptedPassword);
        /*if email contains @polito.it user is a teacher and add a new teacher to db*/
        if(data.getEmail().contains("@polito.it")) {
            user.setRoles(Arrays.asList("ROLE_TEACHER"));
            TeacherDTO teacherDTO = new TeacherDTO(data.getId(),data.getLastName(),
                        data.getFirstName(),data.getEmail(),image);
            teamService.addTeacher(teacherDTO);


        }
        /*if email contains @studenti.polito.it user is a student and add a new student to db*/
        else if(data.getEmail().contains("@studenti.polito.it")) {
            user.setRoles(Arrays.asList("ROLE_STUDENT"));
            StudentDTO studentDTO = new StudentDTO(data.getId(),data.getLastName(),
                    data.getFirstName(),data.getEmail(),image);
            teamService.addStudent(studentDTO);
        }
        userRepository.save(user);
        /*create token to confirmation the account*/
        ConfirmationTokenDTO confirmationTokenDTO = new ConfirmationTokenDTO();
        confirmationTokenDTO.setUsername(user.getId());
        Timestamp expiredDate = new Timestamp(Utils.getNow().getTime() + (24*3600000));
        confirmationTokenDTO.setExpiryDate(expiredDate);
        confirmationTokenDTO.setId(UUID.randomUUID().toString());

        if(!addConfirmationToken(confirmationTokenDTO))
            throw  new DuplicateConfirmationToken("Duplicate confirmation token");

        sendConfirmationMail(user.getEmail(), confirmationTokenDTO);
    }

    public String confirmUser(String  confirmationTokenId) {
        Optional<ConfirmationToken> tokenOptional = confirmationTokenRepository.findById(confirmationTokenId);
        if (!tokenOptional.isPresent()) {
            throw new SecurityServiceException("Expired session");
        }
        if(tokenOptional.get().getExpiryDate().before(Utils.getNow()))
            throw new SecurityServiceException("Expired session");

        Optional<User> user = userRepository.findById(tokenOptional.get().getUsername());
        if(!user.isPresent())
            throw new UsernameNotFoundException("User not found");
        user.get().setEnabled(true);
        userRepository.save(user.get());
        confirmationTokenRepository.delete(tokenOptional.get());
        return user.get().getId();
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
        String email = user.get().getEmail();
        if(email.contains("@polito.it")){
            Teacher t = teacherRepository.findByEmail(email).orElse(null);
            if(t!=null)
                teacherRepository.delete(t);
        }else{
            Student s = studentRepository.findByEmail(email).orElse(null);
            if(s!=null)
                studentRepository.delete(s);

        }

        if(user.get().getAuthorities().size()>0){
            for(GrantedAuthority role : user.get().getAuthorities()){
                user.get().getAuthorities().remove(role);
            }

        }
        userRepository.delete(user.get());

    }
}
