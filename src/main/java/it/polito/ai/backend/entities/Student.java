package it.polito.ai.backend.entities;

import lombok.Data;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class Student {
    @Id
    String id;
    String name;
    String firstName;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "student_course",
            joinColumns = @JoinColumn(name = "student_id"),
            inverseJoinColumns = @JoinColumn(name = "course_name")
    )
    List<Course> courses = new ArrayList<>();

    public void addCourse(Course course) {
        courses.add(course);
        course.students.add(this);
    }

    @ManyToMany(mappedBy = "members")
    List<Team> teams = new ArrayList<>();

    public void addTeam(Team team) {
        teams.add(team);
        team.getMembers().add(this);
    }

    public void removeTeam(Team team) {
        teams.remove(team);
        team.getMembers().remove(this);
    }

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "student_vm",
            joinColumns = @JoinColumn(name = "student_id"),
            inverseJoinColumns = @JoinColumn(name = "vm_id")
    )
    List<VirtualMachine> virtual_machines = new ArrayList<>();
}
