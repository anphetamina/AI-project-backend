package it.polito.ai.backend.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExerciseDTO extends RepresentationModel<ExerciseDTO> {
    // todo add descriptions
    @NotNull
    Long id;
    @NotNull
    Timestamp published;
    @NotNull
    Timestamp expired;

    private Byte[] image;
}
