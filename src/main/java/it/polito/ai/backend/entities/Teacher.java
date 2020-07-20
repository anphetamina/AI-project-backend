package it.polito.ai.backend.entities;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Teacher {
    @Id
    @EqualsAndHashCode.Include
    private String id;
    @NotBlank(message = "The name is mandatory")
    private String name;
    @NotBlank(message = "The firstName is mandatory")
    private String firstName;
    @Email
    private String email;
    //Storing the image (bytes) in the database
    @Lob
    @Basic(fetch = FetchType.LAZY)
    private byte[] profilePicture;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "teacher_course",
            joinColumns = @JoinColumn(name = "teacher_id"),
            inverseJoinColumns = @JoinColumn(name = "course_id")
    )
    List<Course> courses = new ArrayList<>();

    public void addCourse(Course course) {
        courses.add(course);
        course.teachers.add(this);
    }
}
