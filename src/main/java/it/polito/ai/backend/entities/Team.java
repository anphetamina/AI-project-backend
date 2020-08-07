package it.polito.ai.backend.entities;

import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Team {
    @Id
    @GeneratedValue
    @EqualsAndHashCode.Include
    Long id;
    String name;
    TeamStatus status;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "course_id")
    Course course;

    public void setCourse(Course course) {
        if (this.course != null) {
            this.course.teams.remove(this);
        }
        this.course = course;
        if (course != null) {
            course.teams.add(this);
        }
    }

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "team_student",
            joinColumns = @JoinColumn(name = "team_id"),
            inverseJoinColumns = @JoinColumn(name = "student_id")
    )
    List<Student> members = new ArrayList<>();

    public void addStudent(Student student) {
        members.add(student);
        student.teams.add(this);
    }

    public void removeStudent(Student student) {
        members.remove(student);
        student.teams.remove(this);
    }

    public void setStatus(TeamStatus status) {
        this.status = status;

    }

    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    @JoinColumn(name = "configuration")
    Configuration configuration;

    public void setConfiguration(Configuration configuration) {
        if (this.configuration != null) {
            this.configuration.team = null;
        }
        this.configuration = configuration;
        if (configuration != null) {
            configuration.team = this;
        }
    }

    @OneToMany(mappedBy = "team", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    List<VirtualMachine> virtualMachines = new ArrayList<>();

    public void addVirtualMachine(VirtualMachine virtualMachine) {
        virtualMachines.add(virtualMachine);
        virtualMachine.team = this;
    }

    public void removeVirtualMachine(VirtualMachine virtualMachine) {
        virtualMachines.remove(virtualMachine);
        virtualMachine.team = null;
    }
}
