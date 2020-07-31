package it.polito.ai.backend.dtos;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import it.polito.ai.backend.entities.VirtualMachineStatus;
import lombok.*;
import org.springframework.hateoas.RepresentationModel;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class VirtualMachineDTO extends RepresentationModel<VirtualMachineDTO> {
    @EqualsAndHashCode.Include
    Long id;
    @Positive @Schema(description = "cpu cores number") int num_vcpu;
    @Positive @Schema(description = "amount of disk space (MB)") int disk_space;
    @Positive @Schema(description = "amount of ram (GB)") int ram;

    @NotBlank @Schema(description = "the id of the authenticated student that will be added as owner") String studentId;
    @NotNull Long teamId;
    @NotNull Long modelId;

}
