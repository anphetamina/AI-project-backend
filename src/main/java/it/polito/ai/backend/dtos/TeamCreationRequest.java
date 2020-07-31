package it.polito.ai.backend.dtos;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;


@Data
public class TeamCreationRequest {
    @NotBlank String teamName;
    @NotEmpty List<String> memberIds;
    @NotBlank String studentId;
    @NotBlank String timeout;
}
