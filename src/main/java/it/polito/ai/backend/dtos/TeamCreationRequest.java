package it.polito.ai.backend.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;


@Data
public class TeamCreationRequest {
    @NotBlank @Schema(description = "name of team") String teamName;
    @NotEmpty @Schema(description = "list of participant without the proponent") List<String> memberIds;
    @NotBlank @Schema(description = "id of proponent") String studentId;
    @NotBlank @Schema(description = "date(dd/mm/yyyy) when the prose is expired ") String timeout;
}
