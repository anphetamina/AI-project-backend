package it.polito.ai.backend.entities;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;

@Entity
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class VirtualMachineConfiguration {
    @Id
    @GeneratedValue
    @EqualsAndHashCode.Include
    Long id;

    /**
     * minimum values per vm
     */
    int min_vcpu;
    int min_disk_space;
    int min_ram;

    /**
     * maximum values per vms
     */
    int max_vcpu;
    int max_disk_space;
    int max_ram;

    /**
     * total number of vms both active and inactive
     */
    int tot;

    /**
     * total number of active vms
     */
    int max_on;

    @OneToOne(mappedBy = "vm_configuration")
    Team team;

    public void setTeam(Team team) {
        if (this.team != null) {
            this.team.vm_configuration = null;
        }
        this.team = team;
        if (team != null) {
            team.vm_configuration = this;
        }
    }
}
