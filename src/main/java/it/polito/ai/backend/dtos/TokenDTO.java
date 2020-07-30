package it.polito.ai.backend.dtos;

import it.polito.ai.backend.entities.TokenStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;

import javax.persistence.Id;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TokenDTO extends RepresentationModel<TokenDTO> {
    @NotBlank
    String id;
    @NotNull
    Long teamId;
    @NotBlank
    String studentId;
    @NotNull
    @Min(0)
    @Max(1)
    TokenStatus status;
    @NotNull
    Timestamp expiryDate;
}
