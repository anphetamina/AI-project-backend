package it.polito.ai.backend.entities;

import lombok.Data;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class VirtualMachine {

    @Id
    @GeneratedValue
    Long id;
    int num_vcpu;
    int disk_space;
    int ram;
    VirtualMachineStatus status;

    @ManyToMany(mappedBy = "virtual_machines")
    List<Student> owners = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "course_id")
    Course course;

    @ManyToOne
    @JoinColumn(name = "team_id")
    Team team;
}
