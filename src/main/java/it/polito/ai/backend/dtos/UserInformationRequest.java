package it.polito.ai.backend.dtos;


import io.swagger.v3.oas.annotations.media.Schema;
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
    @NotBlank @Schema(description = "id of user") String id;
    @Email @Schema(description = "email of user") private String email;
    @NotBlank @Schema(description = "last name of user") private String lastName;
    @NotBlank @Schema(description = "firts name of user") private String firstName;
    @NotBlank
    @Pattern(regexp= "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$")
    @Schema(description = " Password must be:\n" +
            "     * At least 8 chars\n" +
            "     * Contains at least one digit\n" +
            "     * Contains at least one lower alpha char and one upper alpha char\n" +
            "     * Contains at least one char within a set of special chars (@#%$^ etc.)\n" +
            "     * Does not contain space, tab, etc.")
    String password;
    @NotBlank @Schema(description = " must be equal with the password ")
    String repeatPassword;

    /** Password must be:
     * At least 8 chars
     * Contains at least one digit
     * Contains at least one lower alpha char and one upper alpha char
     * Contains at least one char within a set of special chars (@#%$^ etc.)
     * Does not contain space, tab, etc.
     * */
}
