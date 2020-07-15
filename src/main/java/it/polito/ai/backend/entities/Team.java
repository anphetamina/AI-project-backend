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
        /*switch (status) {
            case ACTIVE:
                this.status = 1;
                break;
            case UNCONFIRMED:
                this.status = 0;
                break;
        }*/
    }

    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    @JoinColumn(name = "vm_conf")
    VirtualMachineConfiguration virtualMachineConfiguration;

    public void setVirtualMachineConfiguration(VirtualMachineConfiguration virtualMachineConfiguration) {
        if (this.virtualMachineConfiguration != null) {
            virtualMachineConfiguration.team = null;
        }
        this.virtualMachineConfiguration = virtualMachineConfiguration;
        if (virtualMachineConfiguration != null) {
            virtualMachineConfiguration.team = this;
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
