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
@Relation(collectionRelation = "paperList", itemRelation = "paper")
public class PaperDTO extends RepresentationModel<PaperDTO> {
    Long id;
    @NotNull  @Schema(description = "date(dd/mm/yyyy) when was published") Timestamp published;
    @NotNull  @Schema(description = "the state, can be NULL, LETTO, CONSEGANTO, RIVISTO")
    PaperStatus status;
    @NotNull @Schema(description = "if true the student che upload an assignment, it is set to false when teacher assignmet an score") boolean flag;
    @Schema(description = "is a string with an opinion/score assigned by the teacher, can be null") String score;
    @Schema(description = "an array of byte of the image uploaded") private byte[] image;
}
