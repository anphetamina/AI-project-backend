package it.polito.ai.backend.entities;

import it.polito.ai.backend.services.exercise.AssignmentStatus;
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
    /*true lo studente può caricare un assignment
      false lo studente non può carricare */
    boolean flag;
    /* null oppure tra 18 e 30*/
    Integer score;
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