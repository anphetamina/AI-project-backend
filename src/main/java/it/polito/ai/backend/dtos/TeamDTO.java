package it.polito.ai.backend.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;

import javax.persistence.Id;
import javax.validation.constraints.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TeamDTO extends RepresentationModel<TeamDTO> {
    // @NotNull
    Long id;
    @NotBlank
    String name;
    @NotNull
    @Min(0)
    @Max(1)
    Integer status; // todo change with TeamStatus?
}
