package it.polito.ai.backend.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Relation(collectionRelation = "configurationList", itemRelation = "configuration")
public class ConfigurationDTO extends RepresentationModel<ConfigurationDTO> {

    Long id;
    @Positive @Schema(description = "cpu cores number for a single virtual machine") int min_vcpu;
    @Positive @Schema(description = "max cpu cores number for all virtual machines") int max_vcpu;
    @Positive @Schema(description = "disk space amount (MB) for a single virtual machine") int min_disk;
    @Positive @Schema(description = "max disk space amount (MB) for all virtual machines") int max_disk;
    @Positive @Schema(description = "ram amount (GB) for a single virtual machine") int min_ram;
    @Positive @Schema(description = "max ram amount (GB) for all virtual machines") int max_ram;

    @Positive @Schema(description = "total number of virtual machines both active and inactive") int tot;

    @Positive @Schema(description = "total number of active virtual machines") int max_on;

    @NotNull Long teamId;
}
