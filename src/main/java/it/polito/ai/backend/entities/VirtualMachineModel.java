package it.polito.ai.backend.entities;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;

@Entity
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class VirtualMachineModel {

    @Id
    @GeneratedValue
    @EqualsAndHashCode.Include
    Long id;
    /**
     * min-max resources per vm
     */
    int min_vcpu;
    int max_vcpu;
    int min_disk;
    int max_disk;
    int min_ram;
    int max_ram;

    /**
     * tot max resources per vms
     */
    int tot_vcpu;
    int tot_disk;
    int tot_ram;

    /**
     * total number of vms both active and inactive
     */
    int tot;

    /**
     * total number of active vms
     */
    int max_on;

    @OneToOne(mappedBy = "vm_model")
    Course course;
}
