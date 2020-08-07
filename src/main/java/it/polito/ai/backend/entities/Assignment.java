package it.polito.ai.backend.entities;

import it.polito.ai.backend.dtos.AssignmentStatus;
import lombok.Data;

import javax.persistence.*;
import java.sql.Timestamp;



@Entity
@Data
public class Assignment {
    @Id
    @GeneratedValue
    Long id;
    Timestamp published;
    AssignmentStatus status;
    boolean flag;

    String score;
    @Lob
    private Byte[] image;
    @ManyToOne
    @JoinColumn(name = "exercise_id")
    Exercise exercise;

    public void setExercise(Exercise exercise) {
        if (this.exercise != null) {
            this.exercise.getAssignments().remove(this);
        }
        this.exercise = exercise;
        if (exercise != null) {
            exercise.getAssignments().add(this);

        }
    }
    @ManyToOne
    @JoinColumn(name = "student")
    Student student;
    public void setStudent(Student student) {
        if (this.student != null) {
            this.student.getAssignments().remove(this);
        }
        this.student=student;
        if (student != null) {
            student.getAssignments().add(this);
        }
    }


}