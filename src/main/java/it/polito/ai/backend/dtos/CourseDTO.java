package it.polito.ai.backend.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;

import javax.persistence.Id;
import javax.validation.constraints.*;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CourseDTO extends RepresentationModel<CourseDTO> {
    @NotBlank
    @Schema(description = "course name abbreviation")
    String id;
    @NotBlank
    @Schema(description = "course name")
    String name;
    @Positive
    @Schema(description = "the min size of students per team")
    int min;
    @Positive
    @Schema(description = "the max size of students per team")
    int max;
    @NotNull
    @Schema(description = "course status")
    Boolean enabled;
    @NotBlank
    @Schema(description = "course creator")
    String teacherId;
}
