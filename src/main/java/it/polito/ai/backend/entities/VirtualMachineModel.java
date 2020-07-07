package it.polito.ai.backend.entities;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;

@Entity
@Data
public class VirtualMachineModel {

    @Id
    @GeneratedValue
    Long id;
    int num_vcpu;
    int disk_space;
    int ram;

    @OneToOne(mappedBy = "configuration")
    Course course;
}
