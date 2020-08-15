package it.polito.ai.backend.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class PaperRequest {
    @NotBlank  @Schema(description = "id of student") String studentId;
    @NotNull  @Schema(description = "if true the student can update an assignment") boolean flag;
    @NotBlank @Schema(description = "is a string with an opinion/score assigned by the teacher, can be null") String score;
}
