package it.polito.ai.backend.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;

import javax.persistence.Id;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TokenDTO extends RepresentationModel<TokenDTO> {
    @Id
    @NotBlank
    String id;
    @NotNull
    Long teamId;
    @NotNull
    Timestamp expiryDate;
}
