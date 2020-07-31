package it.polito.ai.backend.dtos;


import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class ExerciseCreationRequest {
    @NotBlank String expired;
}
