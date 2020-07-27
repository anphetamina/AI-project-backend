package it.polito.ai.backend.controllers;

import io.swagger.v3.oas.annotations.Parameter;
import it.polito.ai.backend.dtos.StudentDTO;
import it.polito.ai.backend.dtos.TeamDTO;
import it.polito.ai.backend.dtos.VirtualMachineDTO;
import it.polito.ai.backend.dtos.VirtualMachineModelDTO;
import it.polito.ai.backend.services.vm.VirtualMachineNotFoundException;
import it.polito.ai.backend.services.vm.VirtualMachineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/API/virtual-machines")
@Validated
public class VirtualMachineController {

    @Autowired
    VirtualMachineService virtualMachineService;

    @GetMapping("/{vmId}")
    VirtualMachineDTO getOne(@PathVariable @NotNull Long vmId) {
        VirtualMachineDTO virtualMachineDTO = virtualMachineService.getVirtualMachine(vmId)
                .orElseThrow(() -> new VirtualMachineNotFoundException(vmId.toString()));
        Long teamId = virtualMachineService.getTeamForVirtualMachine(vmId).map(TeamDTO::getId).orElse(null);
        Long modelId = virtualMachineService.getVirtualMachineModelForVirtualMachine(vmId).map(VirtualMachineModelDTO::getId).orElse(null);
        return ModelHelper.enrich(virtualMachineDTO, teamId, modelId);
    }

    @GetMapping("/{vmId}/owners")
    CollectionModel<StudentDTO> getOwners(@PathVariable @NotNull Long vmId) {
        List<StudentDTO> studentDTOList = virtualMachineService.getOwnersForVirtualMachine(vmId)
                .stream()
                .map(ModelHelper::enrich)
                .collect(Collectors.toList());
        Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(VirtualMachineController.class).getOwners(vmId)).withSelfRel();
        return CollectionModel.of(studentDTOList, selfLink);
    }

    @PostMapping({"", "/"})
    @ResponseStatus(HttpStatus.CREATED)
    VirtualMachineDTO addVirtualMachine(@RequestBody @Valid VirtualMachineDTO virtualMachineDTO) {
        String studentId = virtualMachineDTO.getStudentId();
        Long teamId = virtualMachineDTO.getTeamId();
        Long modelId = virtualMachineDTO.getModelId();
        VirtualMachineDTO virtualMachine = virtualMachineService.createVirtualMachine(studentId, teamId, modelId, virtualMachineDTO);
        return ModelHelper.enrich(virtualMachine, teamId, modelId);
    }

    @DeleteMapping("/{vmId}")
    void deleteVirtualMachine(@PathVariable @NotNull Long vmId) {
        if (!virtualMachineService.deleteVirtualMachine(vmId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "please turn off the virtual machine before deleting it");
        }

    }

    @PutMapping("/{vmId}")
    VirtualMachineDTO setVirtualMachine(@RequestBody @Valid VirtualMachineDTO virtualMachineDTO, @PathVariable @NotNull Long vmId) {
        VirtualMachineDTO virtualMachineDTO1 = virtualMachineService.updateVirtualMachine(vmId, virtualMachineDTO);
        Long teamId = virtualMachineService.getTeamForVirtualMachine(vmId).map(TeamDTO::getId).orElse(null);
        Long modelId = virtualMachineService.getVirtualMachineModelForVirtualMachine(vmId).map(VirtualMachineModelDTO::getId).orElse(null);
        return ModelHelper.enrich(virtualMachineDTO1, teamId, modelId);
    }

    @PostMapping("/{vmId}/on")
    void turnOn(@PathVariable @NotNull Long vmId) {
        virtualMachineService.turnOnVirtualMachine(vmId);
    }

    @PostMapping("/{vmId}/off")
    void turnOff(@PathVariable @NotNull Long vmId) {
        virtualMachineService.turnOffVirtualMachine(vmId);
    }

    @PostMapping("/{vmId}/owners")
    void shareOwnership(@PathVariable @NotNull Long vmId, @Parameter(example = "studentId") @RequestBody HashMap<String, String> map) {
        if (!map.containsKey("studentId")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        String studentId = map.get("studentId");

        /**
         * if studentId is null, the service will throw a StudentNotFoundException but it should be a bad request
         */
        if (studentId == null || studentId.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        if (!virtualMachineService.addOwnerToVirtualMachine(studentId, vmId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "cannot share ownership with "+studentId);
        }
    }

}
