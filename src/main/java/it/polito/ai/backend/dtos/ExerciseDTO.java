package it.polito.ai.backend.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;

import javax.persistence.GeneratedValue;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExerciseDTO extends RepresentationModel<ExerciseDTO> {
    /**
     * id autogenrated
     * published is the date when teacher uploaded the exercise
     * expired is the date when exercise expierd and the students can not upload an assignemnat
     * image is an array of bytes of the image uploaded
     * */

    @NotNull
    Long id;
    @NotNull
    Timestamp published;
    @NotNull
    Timestamp expired;

    private Byte[] image;
}
