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
    String id;
    @NotBlank
    String name;
    @Positive @Schema(description = "the min size of students per team")
    int min;
    @Positive @Schema(description = "the max size of students per team")
    int max;
}
