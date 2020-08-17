package it.polito.ai.backend.dtos;

import com.opencsv.bean.CsvBindByName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import javax.persistence.Id;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Relation(collectionRelation = "studentList", itemRelation = "student")
public class StudentDTO extends RepresentationModel<StudentDTO> {
    @CsvBindByName
    @NotBlank
    String id;
    @CsvBindByName(column = "first_name")
    @NotBlank
    String firstName;
    @CsvBindByName(column = "last_name")
    @NotBlank
    String lastName;
    @Email
    private String email;
    private Byte[] image;
}
