package it.polito.ai.backend;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import it.polito.ai.backend.entities.User;
import it.polito.ai.backend.repositories.UserRepository;
import it.polito.ai.backend.services.notification.NotificationService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;

@SpringBootApplication
public class BackendApplication {

    @Bean
    ModelMapper modelMapper() {
        return new ModelMapper();
    }

    @Bean
    public OpenAPI customOpenAPI(@Value("${springdoc.version}") String appVersion) {
        return new OpenAPI()
                .info(new Info()
                        .title("AI project backend API")
                        .version(appVersion)
                        .description("API documentation for the AI project backend"))
                .components(new Components().addSecuritySchemes("bearer-jwt",
                        new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")
                                .in(SecurityScheme.In.HEADER).name("Authorization")));
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    CommandLineRunner run(UserRepository users, PasswordEncoder passwordEncoder){
        return  new CommandLineRunner() {
            @Override
            public void run(String... args) throws Exception {
                try{

                    users.save(User.builder()
                            .username("d123456@polito.it")
                            .password(passwordEncoder.encode("password"))
                            .roles(Arrays.asList("ROLE_TEACHER")).build());
                    users.save(User.builder()
                            .username("d123457@polito.it")
                            .password(passwordEncoder.encode("password"))
                            .roles(Arrays.asList("ROLE_TEACHER")).build());
                    users.save(User.builder()
                            .username("d123458@polito.it")
                            .password(passwordEncoder.encode("password"))
                            .roles(Arrays.asList("ROLE_TEACHER")).build());
                    users.save(User.builder()
                            .username("s123459@studenti.polito.it")
                            .password(passwordEncoder.encode("studente"))
                            .roles(Arrays.asList("ROLE_STUDENT")).build());
                    users.save(User.builder()
                            .username("s123461@studenti.polito.it")
                            .password(passwordEncoder.encode("studente"))
                            .roles(Arrays.asList("ROLE_STUDENT")).build());

                    System.out.println("printing all users...");
                    users.findAll().
                            forEach(v-> System.out.println("User "+ v.toString()));

                }catch (Exception e){
                    System.out.println(e.getMessage());
                }



            }
        };
    }
    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

}
