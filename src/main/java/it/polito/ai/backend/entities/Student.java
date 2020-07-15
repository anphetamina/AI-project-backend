package it.polito.ai.backend.entities;

import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Student {

    @Id
    @EqualsAndHashCode.Include
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

    @ManyToMany(mappedBy = "members", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    List<Team> teams = new ArrayList<>();

    public void addTeam(Team team) {
        teams.add(team);
        team.members.add(this);
    }

    public void removeTeam(Team team) {
        teams.remove(team);
        team.members.remove(this);
    }

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "student_vm",
            joinColumns = @JoinColumn(name = "student_id"),
            inverseJoinColumns = @JoinColumn(name = "vm_id")
    )
    List<VirtualMachine> virtual_machines = new ArrayList<>();

    public void addVirtualMachine(VirtualMachine vm) {
        virtual_machines.add(vm);
        vm.owners.add(this);
    }

    public void removeVirtualMachine(VirtualMachine vm) {
        virtual_machines.remove(vm);
        vm.owners.remove(this);
    }
}
