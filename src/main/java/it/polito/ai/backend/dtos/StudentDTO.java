package it.polito.ai.backend.dtos;

import com.opencsv.bean.CsvBindByName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;

import javax.persistence.Id;
import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StudentDTO extends RepresentationModel<StudentDTO> {
    @Id
    @CsvBindByName
    @NotBlank
    String id;
    @CsvBindByName
    @NotBlank
    String name;
    @CsvBindByName(column = "first_name")
    @NotBlank
    String firstName;
}
