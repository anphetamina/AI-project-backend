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
    @NotNull SystemImage system_image;

    // todo add more fields

    @ManyToOne
    @JoinColumn(name = "team_id")
    Team team;

    public void setTeam(Team team) {
        if (this.team != null) {
            this.team.vm_models.remove(this);
        }
        this.team = team;
        if (team != null) {
            team.vm_models.add(this);
        }
    }

    @OneToMany(mappedBy = "vm_model", cascade = CascadeType.ALL, orphanRemoval = true)
    List<VirtualMachine> virtual_machines = new ArrayList<>();

    public void addVirtualMachine(VirtualMachine vm) {
        virtual_machines.add(vm);
        vm.vm_model = this;
    }

    public void removeVirtualMachine(VirtualMachine vm) {
        virtual_machines.remove(vm);
        vm.vm_model = null;
    }
}
