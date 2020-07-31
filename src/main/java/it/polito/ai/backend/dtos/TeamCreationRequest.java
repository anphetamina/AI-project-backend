package it.polito.ai.backend.dtos;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

// todo use
@Data
public class TeamCreationRequest {
    @NotBlank String teamName;
    @NotEmpty List<String> memberIds;
}
