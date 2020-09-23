package it.polito.ai.backend.controllers;

import io.swagger.v3.oas.annotations.Operation;
import it.polito.ai.backend.dtos.*;
import it.polito.ai.backend.security.SecurityService;
import it.polito.ai.backend.services.vm.VirtualMachineNotFoundException;
import it.polito.ai.backend.services.vm.VirtualMachineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/API/virtual-machines")
@Validated
public class VirtualMachineController {

    @Autowired
    VirtualMachineService virtualMachineService;

    @Operation(summary = "get virtual machine")
    @GetMapping("/{vmId}")
    ResponseEntity<VirtualMachineDTO> getOne(@PathVariable @NotNull Long vmId) {
        VirtualMachineDTO virtualMachineDTO = virtualMachineService.getVirtualMachine(vmId)
                .orElseThrow(() -> new VirtualMachineNotFoundException(vmId.toString()));
        Long teamId = virtualMachineService.getTeamForVirtualMachine(vmId).map(TeamDTO::getId).orElse(null);
        Long modelId = virtualMachineService.getVirtualMachineModelForVirtualMachine(vmId).map(VirtualMachineModelDTO::getId).orElse(null);
        return new ResponseEntity<>(ModelHelper.enrich(virtualMachineDTO, teamId, modelId),HttpStatus.OK);
    }

    @Operation(summary = "get owners of a virtual machine")
    @GetMapping("/{vmId}/owners")
    ResponseEntity<CollectionModel<StudentDTO>> getOwners(@PathVariable @NotNull Long vmId) {
        List<StudentDTO> studentDTOList = virtualMachineService.getOwnersForVirtualMachine(vmId)
                .stream()
                .map(ModelHelper::enrich)
                .collect(Collectors.toList());
        Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(VirtualMachineController.class).getOwners(vmId)).withSelfRel();
        return new ResponseEntity<>(CollectionModel.of(studentDTOList, selfLink),HttpStatus.OK);
    }

    @Operation(summary = "create a new virtual machine")
    @PostMapping({"", "/"})
    @ResponseStatus(HttpStatus.CREATED)
    ResponseEntity<VirtualMachineDTO> addVirtualMachine(@RequestBody @Valid VirtualMachineDTO virtualMachineDTO) {
        String studentId = virtualMachineDTO.getStudentId();
        Long teamId = virtualMachineDTO.getTeamId();
        Long modelId = virtualMachineDTO.getModelId();
        VirtualMachineDTO virtualMachine = virtualMachineService.createVirtualMachine(studentId, teamId, modelId, virtualMachineDTO);
        return new ResponseEntity<>(ModelHelper.enrich(virtualMachine, teamId, modelId),HttpStatus.OK);
    }

    @Operation(summary = "delete an existing virtual machine")
    @DeleteMapping("/{vmId}")
    void deleteVirtualMachine(@PathVariable @NotNull Long vmId) {
        if (!virtualMachineService.deleteVirtualMachine(vmId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "please turn off the virtual machine before deleting it");
        }

    }

    @Operation(summary = "update an existing virtual machine")
    @PutMapping("/{vmId}")
    ResponseEntity<VirtualMachineDTO> setVirtualMachine(@RequestBody @Valid VirtualMachineDTO virtualMachineDTO, @PathVariable @NotNull Long vmId) {
        VirtualMachineDTO virtualMachineDTO1 = virtualMachineService.updateVirtualMachine(vmId, virtualMachineDTO);
        // Long teamId = virtualMachineService.getTeamForVirtualMachine(vmId).map(TeamDTO::getId).orElse(null);
        // Long modelId = virtualMachineService.getVirtualMachineModelForVirtualMachine(vmId).map(VirtualMachineModelDTO::getId).orElse(null);
        return new ResponseEntity<>(ModelHelper.enrich(virtualMachineDTO1,
                virtualMachineDTO.getTeamId(), virtualMachineDTO.getModelId()),HttpStatus.OK);
    }

    @Operation(summary = "turn on a virtual machine")
    @PostMapping("/{vmId}/on")
    void turnOn(@PathVariable @NotNull Long vmId) {
        virtualMachineService.turnOnVirtualMachine(vmId);
    }

    @Operation(summary = "turn off a virtual machine")
    @PostMapping("/{vmId}/off")
    void turnOff(@PathVariable @NotNull Long vmId) {
        virtualMachineService.turnOffVirtualMachine(vmId);
    }

    @Operation(summary = "add an existing student to the owners of a virtual machine")
    @PostMapping("/{vmId}/owners")
    void shareOwnership(@PathVariable @NotNull Long vmId, @RequestBody @Valid OwnershipRequest request) {

        String studentId = request.getStudentId();

        if (!virtualMachineService.addOwnerToVirtualMachine(studentId, vmId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "cannot share ownership with "+studentId);
        }
    }

}
