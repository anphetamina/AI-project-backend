package it.polito.ai.backend.dtos;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Data
@Builder
public class VirtualMachineConfigurationDTO {

    @NotNull
    Long id;
    @Positive
    int num_vcpu;
    @Positive
    int disk_space;
    @Positive
    int ram;
}
