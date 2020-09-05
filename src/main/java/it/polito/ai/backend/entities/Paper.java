package it.polito.ai.backend.entities;

import it.polito.ai.backend.dtos.PaperStatus;
import lombok.Data;

import javax.persistence.*;
import java.sql.Timestamp;



@Entity
@Data
public class Paper {
    @Id
    @GeneratedValue
    Long id;
    Timestamp published;
    PaperStatus status;
    boolean flag;

    String score;
    @Lob
    private byte[] image;
    @ManyToOne
    @JoinColumn(name = "assignment_id")
    Assignment assignment;

    public void setAssignment(Assignment assignment) {
        if (this.assignment != null) {
            this.assignment.getPapers().remove(this);
        }
        this.assignment = assignment;
        if (assignment != null) {
            assignment.getPapers().add(this);

        }
    }
    @ManyToOne
    @JoinColumn(name = "student")
    Student student;
    public void setStudent(Student student) {
        if (this.student != null) {
            this.student.getPapers().remove(this);
        }
        this.student=student;
        if (student != null) {
            student.getPapers().add(this);
        }
    }


}