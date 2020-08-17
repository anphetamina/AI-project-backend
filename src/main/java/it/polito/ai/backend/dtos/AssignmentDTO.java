package it.polito.ai.backend.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import javax.validation.constraints.NotNull;
import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Relation(collectionRelation = "assignmentList", itemRelation = "assignment")
public class AssignmentDTO extends RepresentationModel<AssignmentDTO> {
    Long id;
    @NotNull @Schema(description = "date(dd/mm/yyyy) when teacher uploaded the exercise") Timestamp published;
    @NotNull @Schema(description = "date(dd/mm/yyyy) when exercise expired and the students can not upload an assignment") Timestamp expired;
    @Schema(description = "an array of byte of the image uploaded") private Byte[] image;
}
