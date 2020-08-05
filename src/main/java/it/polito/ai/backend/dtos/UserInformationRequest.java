package it.polito.ai.backend.dtos;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.Email;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

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
    @Pattern(regexp= "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$")
    String password;
    @NotBlank
    String repeatPassword;

    /** Password must be:
     * At least 8 chars
     * Contains at least one digit
     * Contains at least one lower alpha char and one upper alpha char
     * Contains at least one char within a set of special chars (@#%$^ etc.)
     * Does not contain space, tab, etc.
     * */
}
