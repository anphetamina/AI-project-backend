package it.polito.ai.backend.entities;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Course {
    @Id
    @EqualsAndHashCode.Include
    String id;
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

    @OneToOne
    @JoinColumn(name = "vm_model")
    VirtualMachineModel vm_model;

    /*@OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    List<VirtualMachine> virtual_machines = new ArrayList<>();*/
}
