package it.polito.ai.backend.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import it.polito.ai.backend.services.exercise.AssignmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;

import javax.persistence.GeneratedValue;
import javax.validation.constraints.NotNull;
import java.sql.Timestamp;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AssignmentDTO extends RepresentationModel<AssignmentDTO> {
    Long id;
    @NotNull  @Schema(description = "date(dd/mm/yyyy) when was published") Timestamp published;
    @NotNull  @Schema(description = "the state, can be NULL, LETTO, CONSEGANTO, RIVISTO") AssignmentStatus status;
    @NotNull @Schema(description = "if true the student che upload an assignment, it is set to false when teacher assignmet an score") boolean flag;
    @Schema(description = "score is the score assigned by the teacher, can be null") Integer score;
    @Schema(description = "an array of byte of the image uploaded") private Byte[] image;
}
