package it.polito.ai.backend.dtos;

import it.polito.ai.backend.entities.VirtualMachineStatus;
import lombok.*;
import org.springframework.hateoas.RepresentationModel;

import javax.validation.constraints.Positive;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class VirtualMachineDTO extends RepresentationModel<VirtualMachineDTO> {
    @EqualsAndHashCode.Include Long id;
    @Positive int num_vcpu;
    @Positive int disk_space;
    @Positive int ram;

    VirtualMachineStatus status;

}
