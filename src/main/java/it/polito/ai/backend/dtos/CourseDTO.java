package it.polito.ai.backend.dtos;

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
    String name;
    @Positive
    int min;
    @Positive
    int max;
    @NotNull
    Boolean enabled;
}
