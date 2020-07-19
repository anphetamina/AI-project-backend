package it.polito.ai.backend.entities;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Student {
    // todo add photo

    @Id
    @EqualsAndHashCode.Include
    String id;
    String name;
    String firstName;
    //byte[] profilePicture;

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

    @OneToMany(mappedBy = "student"/*, cascade = CascadeType.ALL, orphanRemoval = true*/)
    List<Assignment> assignments = new ArrayList<Assignment>();
    public void addAssignment(Assignment assignment) {
        assignment.student = this;
        assignments.add(assignment);
        assignment.setStudent(this);
    }

}
