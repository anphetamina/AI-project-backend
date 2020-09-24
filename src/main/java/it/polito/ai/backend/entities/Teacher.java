package it.polito.ai.backend.entities;


import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Teacher {
    @Id
    @EqualsAndHashCode.Include
    private String id;
    @NotBlank(message = "The firstName is mandatory")
    private String firstName;
    @NotBlank(message = "The lastName is mandatory")
    private String lastName;
    @Email
    private String email;
    //Storing the image (bytes) in the database
    @Lob
    private byte[] image;

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

    public void removeCourse(Course course) {
        courses.remove(course);
        course.teachers.remove(this);
    }
}
