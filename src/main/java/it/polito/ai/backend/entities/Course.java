package it.polito.ai.backend.entities;

import lombok.Data;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class Course {
    @Id
    String name;
    int min;
    int max;
    boolean enabled;

    @ManyToMany(mappedBy = "courses")
    List<Student> students = new ArrayList<Student>();

    public void addStudent(Student student) {
        students.add(student);
        student.courses.add(this);
    }

    @ManyToMany(mappedBy = "courses", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    List<Teacher> teachers = new ArrayList<Teacher>();

    public void addTeacher(Teacher teacher) {
        teachers.add(teacher);
        teacher.courses.add(this);
    }

    @OneToMany(mappedBy = "course"/*, cascade = CascadeType.ALL, orphanRemoval = true*/)
    List<Team> teams = new ArrayList<Team>();

    public void addTeam(Team team) {
        if (team.course != null) {
            team.getCourse().getTeams().remove(team);
        }
        team.course = this;
        teams.add(team);
        // team.setCourse(this)
    }

    public void removeTeam(Team team) {
        if (team.course != null) {
            team.course = null;
        }
        teams.remove(team);
        // team.setCourse(null)
    }
}
