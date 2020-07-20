package it.polito.ai.backend.dtos;

import it.polito.ai.backend.entities.SystemImage;
import lombok.Data;
import org.springframework.hateoas.RepresentationModel;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Data
public class VirtualMachineModelDTO extends RepresentationModel<VirtualMachineModelDTO> {
    @Positive long id;
    @NotNull SystemImage os;
}
