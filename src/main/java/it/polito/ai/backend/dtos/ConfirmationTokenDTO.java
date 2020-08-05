package it.polito.ai.backend.dtos;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;


import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConfirmationTokenDTO extends RepresentationModel<ConfirmationTokenDTO> {
    @NotBlank
    String id;
    @NotBlank
    String username;
    @NotNull
    Timestamp expiryDate;


}
