package it.polito.ai.backend.dtos;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Data
@Builder
public class VirtualMachineModelDTO {
    @NotNull Long id;
    @Positive int min_vcpu;
    @Positive int max_vcpu;
    @Positive int min_disk;
    @Positive int max_disk;
    @Positive int min_ram;
    @Positive int max_ram;

    @Positive int tot_vcpu;
    @Positive int tot_disk;
    @Positive int tot_ram;
    @Positive int tot;
    @Positive int max_on;
}
