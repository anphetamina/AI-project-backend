package it.polito.ai.backend.entities;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Team {
    @Id
    @GeneratedValue
    @EqualsAndHashCode.Include
    Long id;
    String name;
    TeamStatus status;

    @ManyToOne
    @JoinColumn(name = "course_id")
    Course course;

    public void setCourse(Course course) {
        if (this.course != null) {
            this.course.getTeams().remove(this);
        }
        this.course = course;
        if (course != null) {
            course.getTeams().add(this);
        }
    }

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "team_student",
            joinColumns = @JoinColumn(name = "team_id"),
            inverseJoinColumns = @JoinColumn(name = "student_id")
    )
    List<Student> members = new ArrayList<Student>();

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

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
    List<VirtualMachine> virtual_machines = new ArrayList<>();

    public void addVM(VirtualMachine v) {
        virtual_machines.add(v);
        v.team = this;
    }

    public void removeVM(VirtualMachine v) {
        virtual_machines.remove(v);
        v.team = null;
    }

    @OneToOne
    @JoinColumn(name = "vm_conf")
    VirtualMachineConfiguration vm_configuration;

    public void setVMConfiguration(VirtualMachineConfiguration vmConfiguration) {
        if (this.vm_configuration != null) {
            vm_configuration.team = null;
        }
        this.vm_configuration = vmConfiguration;
        if (vmConfiguration != null) {
            vmConfiguration.team = this;
        }
    }
}