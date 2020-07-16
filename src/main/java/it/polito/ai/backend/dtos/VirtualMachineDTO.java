package it.polito.ai.backend.dtos;

import lombok.Builder;
import lombok.Data;
import org.springframework.hateoas.RepresentationModel;

import javax.validation.constraints.Positive;

@Data
@Builder
public class VirtualMachineDTO extends RepresentationModel<VirtualMachineDTO> {
    @Positive long id;
    @Positive int num_vcpu;
    @Positive int disk_space;
    @Positive int ram;


}
