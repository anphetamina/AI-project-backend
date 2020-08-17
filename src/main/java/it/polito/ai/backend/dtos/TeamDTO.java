package it.polito.ai.backend.dtos;

import it.polito.ai.backend.entities.TeamStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import javax.persistence.Id;
import javax.validation.constraints.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Relation(collectionRelation = "teamList", itemRelation = "team")
public class TeamDTO extends RepresentationModel<TeamDTO> {
    Long id;
    @NotBlank
    String name;
    @NotNull
    TeamStatus status;
}
