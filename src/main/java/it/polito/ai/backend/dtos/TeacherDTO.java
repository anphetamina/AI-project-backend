package it.polito.ai.backend.dtos;

import lombok.AllArgsConstructor;
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
@Relation(collectionRelation = "teacherList", itemRelation = "teacher")
public class TeacherDTO extends RepresentationModel<TeacherDTO> {
    @NotBlank
    String id;
    @NotBlank
    String firstName;
    @NotBlank
    String lastName;
    @Email
    private String email;
    private Byte[] image;
}
