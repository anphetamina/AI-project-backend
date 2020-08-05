package it.polito.ai.backend;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import it.polito.ai.backend.entities.Student;
import it.polito.ai.backend.entities.Teacher;
import it.polito.ai.backend.entities.User;
import it.polito.ai.backend.repositories.StudentRepository;
import it.polito.ai.backend.repositories.TeacherRepository;
import it.polito.ai.backend.repositories.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.List;

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
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .in(SecurityScheme.In.HEADER)
                                .name("Authorization")))
                .addSecurityItem(new SecurityRequirement().addList("bearer-jwt", Arrays.asList("read", "write")));
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    CommandLineRunner runner(StudentRepository studentRepository, TeacherRepository teacherRepository, UserRepository userRepository) {
        return args -> {

            if (studentRepository.count() == 0) {
                File mockStudent = new File("./mock/mock_student.csv");
                if (mockStudent.exists() && mockStudent.length() > 0) {
                    Reader reader = new BufferedReader(new FileReader(mockStudent));
                    CsvToBean<Student> csvToBean = new CsvToBeanBuilder(reader)
                            .withType(Student.class)
                            .withIgnoreLeadingWhiteSpace(true)
                            .build();
                    List<Student> students = csvToBean.parse();
                    studentRepository.saveAll(students);
                }
            }

            if (teacherRepository.count() == 0) {
                File mockTeacher = new File("./mock/mock_teacher.csv");
                if (mockTeacher.exists() && mockTeacher.length() > 0) {
                    Reader reader = new BufferedReader(new FileReader(mockTeacher));
                    CsvToBean<Teacher> csvToBean = new CsvToBeanBuilder(reader)
                            .withType(Teacher.class)
                            .withIgnoreLeadingWhiteSpace(true)
                            .build();
                    List<Teacher> teachers = csvToBean.parse();
                    teacherRepository.saveAll(teachers);
                }
            }

            if (userRepository.count() == 0) {
                User studentUser = User.builder()
                        .username("s1@studenti.polito.it")
                        .password(passwordEncoder().encode("password"))
                        .roles(Arrays.asList("ROLE_STUDENT"))
                        .enable(true)
                        .build();

                User teacherUser = User.builder()
                        .username("d1@polito.it")
                        .password(passwordEncoder().encode("password"))
                        .roles(Arrays.asList("ROLE_TEACHER"))
                        .enable(true)
                        .build();

                userRepository.save(studentUser);
                userRepository.save(teacherUser);
            }

        };
    }

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

}
