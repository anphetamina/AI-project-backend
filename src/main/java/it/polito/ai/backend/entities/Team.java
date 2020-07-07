package it.polito.ai.backend.entities;

import lombok.Data;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
public class Team {
    @Id
    @GeneratedValue
    Long id;
    String name;
    TeamStatus status;

    @ManyToOne
    @JoinColumn(name = "course_id")
    Course course;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "team_student",
            joinColumns = @JoinColumn(name = "team_id"),
            inverseJoinColumns = @JoinColumn(name = "student_id")
    )
    List<Student> members = new ArrayList<Student>();

    public void setCourse(Course course) {
        if (this.course != null) {
            this.course.getTeams().remove(this);
        }
        this.course = course;
        if (course != null) {
            course.getTeams().add(this);
        }
    }

    public void addStudent(Student student) {
        members.add(student);
        student.getTeams().add(this);
    }

    public void removeStudent(Student student) {
        members.remove(student);
        student.getTeams().remove(this);
    }

    public void setStatus(TeamStatus status) {
        this.status = status;
        /*switch (status) {
            case ACTIVE:
                this.status = 1;
                break;
            case UNCONFIRMED:
                this.status = 0;
                break;
        }*/
    }
}
