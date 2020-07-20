package it.polito.ai.backend.entities;

import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor
@NoArgsConstructor
public class VirtualMachine {

    @Id
    @GeneratedValue
    @EqualsAndHashCode.Include
    Long id;
    int num_vcpu;
    int disk_space;
    int ram;
    VirtualMachineStatus status;

    @ManyToMany(mappedBy = "virtual_machines", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    List<Student> owners = new ArrayList<>();

    public void addOwner(Student s) {
        owners.add(s);
        s.virtual_machines.add(this);
    }

    public void removeOwner(Student s) {
        owners.remove(s);
        s.virtual_machines.remove(this);
    }

    public void removeOwners() {
        if (owners.size() > 0) {
            for (Student s : owners) {
                s.virtual_machines.remove(this);
            }
            owners.clear();
        }
    }

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "vm_model")
    VirtualMachineModel virtualMachineModel;

    public void setVirtualMachineModel(VirtualMachineModel virtualMachineModel) {
        if (this.virtualMachineModel != null) {
            this.virtualMachineModel.virtualMachines.remove(this);
        }
        this.virtualMachineModel = virtualMachineModel;
        if (virtualMachineModel != null) {
            virtualMachineModel.virtualMachines.add(this);
        }
    }

    /**
     * a student can be part of multiple teams
     * so a link is needed to get the team configuration
     */
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "team_id")
    Team team;

    public void setTeam(Team team) {
        if (this.team != null) {
            this.team.virtualMachines.remove(this);
        }
        this.team = team;
        if (team != null) {
            team.virtualMachines.add(this);
        }
    }
}

