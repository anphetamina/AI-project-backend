package it.polito.ai.backend.entities;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VirtualMachineModel {
    @Id
    @GeneratedValue
    @EqualsAndHashCode.Include
    Long id;
    SystemImage os;


    @OneToMany(mappedBy = "virtualMachineModel", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    List<VirtualMachine> virtualMachines = new ArrayList<>();

    public void addVirtualMachine(VirtualMachine virtualMachine) {
        virtualMachines.add(virtualMachine);
        virtualMachine.virtualMachineModel = this;
    }

    public void removeVirtualMachine(VirtualMachine virtualMachine) {
        virtualMachines.remove(virtualMachine);
        virtualMachine.virtualMachineModel = null;
    }

    public void removeVirtualMachines() {
        /**
         * make all virtual machines related to this model as orphans so they will get deleted from the db
         */
        for (VirtualMachine vm : virtualMachines) {
            vm.virtualMachineModel = null;
            vm.setTeam(null);
            vm.removeOwners();
        }
        virtualMachines.clear();
    }

    @OneToOne(mappedBy = "virtualMachineModel", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    Course course;

    public void setCourse(Course course) {
        if (this.course != null) {
            this.course.virtualMachineModel = null;
        }
        this.course = course;
        if (course != null) {
            course.virtualMachineModel = this;
        }
    }

}
