package it.polito.ai.backend.entities;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;

@Data
@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class VirtualMachineModel {
    @Id
    @GeneratedValue
    @EqualsAndHashCode.Include
    Long id;
    @NotNull OperatingSystem os;

    // todo add more fields

    @OneToOne(mappedBy = "vm_model")
    Team team;

    public void setTeam(Team team) {
        if (this.team != null) {
            this.team.vm_model = null;
        }
        this.team = team;
        if (team != null) {
            team.vm_model = this;
        }
    }

}
