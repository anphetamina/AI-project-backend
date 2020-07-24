package it.polito.ai.backend.entities;

import lombok.Data;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

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

    @OneToMany(mappedBy = "exercise")
    private List<Assignment> assignments =new ArrayList<Assignment>();
    public void addAssignment(Assignment assignment) {
        assignment.exercise=this;
        assignments.add(assignment);

    }

    public void removeAssignment(Assignment assignment) {
        assignments.remove(assignment);
        assignment.exercise = null;
    }
}
