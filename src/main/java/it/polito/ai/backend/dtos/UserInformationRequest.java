package it.polito.ai.backend.dtos;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.Email;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserInformationRequest {
    @NotBlank
    String id;
    @Email
    private String email;
    @NotBlank
    private String lastName;
    @NotBlank
    private String firstName;
    @NotBlank
    String password;
    @NotBlank
    String repeatPassword;

}
