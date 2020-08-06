package it.polito.ai.backend.dtos;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class AssignmentRequest {
    @NotBlank
    String studentId;
    @NotNull
    boolean flag;
    Integer score;
}
