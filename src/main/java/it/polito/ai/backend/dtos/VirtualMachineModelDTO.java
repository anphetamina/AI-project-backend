package it.polito.ai.backend.dtos;

import it.polito.ai.backend.entities.OperatingSystem;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Data
public class VirtualMachineModelDTO {
    @Positive long id;
    @NotNull OperatingSystem os;
}
