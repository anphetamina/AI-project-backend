package it.polito.ai.backend.dtos;

import it.polito.ai.backend.entities.SystemImage;
import lombok.Data;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Data
@Relation(collectionRelation = "virtualMachineModelList", itemRelation = "virtualMachineModel")
public class VirtualMachineModelDTO extends RepresentationModel<VirtualMachineModelDTO> {
    Long id;
    @NotNull SystemImage os;
    @NotBlank String courseId;
}
