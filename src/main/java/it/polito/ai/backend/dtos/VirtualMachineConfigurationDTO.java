package it.polito.ai.backend.dtos;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Data
@Builder
public class VirtualMachineConfigurationDTO {

    @NotNull Long id;
    @Positive int min_vcpu;
    @Positive int max_vcpu;
    @Positive int min_disk;
    @Positive int max_disk;
    @Positive int min_ram;
    @Positive int max_ram;

    /**
     * total number of vms both active and inactive
     */
    @Positive int tot;

    /**
     * total number of active vms
     */
    @Positive int max_on;
}
