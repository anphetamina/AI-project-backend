package it.polito.ai.backend.dtos;

import it.polito.ai.backend.services.exercise.AssignmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;

import javax.validation.constraints.NotNull;
import java.sql.Timestamp;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AssignmentDTO extends RepresentationModel<AssignmentDTO> {
    @NotNull
    Long id;
    @NotNull
    Timestamp published;
    AssignmentStatus status;
    @NotNull
    boolean flag;
    Integer score;
    private Byte[] image;
}
