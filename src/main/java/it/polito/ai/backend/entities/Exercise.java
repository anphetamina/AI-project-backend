package it.polito.ai.backend.entities;

import lombok.Data;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Data
public class Exercise {
    @Id
    @GeneratedValue
    Long id;
    Timestamp published;
    Timestamp expired;
    @Lob
    private Byte[] image;
    @ManyToOne
    @JoinColumn(name = "course_id")
    Course course;

    public void setCourse(Course course) {
        if (this.course != null) {
            this.course.getExercises().remove(this);
        }
        this.course = course;
        if (course != null) {
            course.getExercises().add(this);
        }
    }
}
