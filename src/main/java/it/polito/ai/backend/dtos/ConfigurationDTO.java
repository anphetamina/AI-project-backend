package it.polito.ai.backend.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConfigurationDTO extends RepresentationModel<ConfigurationDTO> {

    Long id;
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

    @NotNull Long teamId;
}
