package it.polito.ai.backend.entities;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
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
    @NotBlank(message = "The firstName is mandatory")
    String name;
    @NotBlank(message = "The firstName is mandatory")
    String firstName;
    @Email
    private String email;
    @Lob
    private Byte[] image;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "student_course",
            joinColumns = @JoinColumn(name = "student_id"),
            inverseJoinColumns = @JoinColumn(name = "course_id")
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

    @OneToMany(mappedBy = "student"/*, cascade = CascadeType.ALL, orphanRemoval = true*/)
    List<Assignment> assignments = new ArrayList<Assignment>();
    public void addAssignment(Assignment assignment) {
        assignment.student = this;
        assignments.add(assignment);
        // assignment.setStudent(this);
    }


    public void addVirtualMachine(VirtualMachine vm) {
        virtual_machines.add(vm);
        vm.owners.add(this);
    }

    public void removeVirtualMachine(VirtualMachine vm) {
        virtual_machines.remove(vm);
        vm.owners.remove(this);
    }
}
