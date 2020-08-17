package it.polito.ai.backend.controllers;

import io.swagger.v3.oas.annotations.Operation;
import it.polito.ai.backend.dtos.CourseDTO;
import it.polito.ai.backend.dtos.VirtualMachineModelDTO;
import it.polito.ai.backend.entities.SystemImage;
import it.polito.ai.backend.services.vm.VirtualMachineModelNotFoundException;
import it.polito.ai.backend.services.vm.VirtualMachineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/API/virtual-machine-models")
@Validated
public class VirtualMachineModelController {

    @Autowired
    VirtualMachineService virtualMachineService;

    @Operation(summary = "get virtual machine model")
    @GetMapping("/{modelId}")
    ResponseEntity<VirtualMachineModelDTO> getOne(@PathVariable @NotNull Long modelId) {
        VirtualMachineModelDTO virtualMachineModelDTO = virtualMachineService.getVirtualMachineModel(modelId)
                .orElseThrow(() -> new VirtualMachineModelNotFoundException(modelId.toString()));
        String courseId = virtualMachineService.getCourseForVirtualMachineModel(modelId).map(CourseDTO::getId).orElse(null);
        return new ResponseEntity<>(ModelHelper.enrich(virtualMachineModelDTO, courseId),HttpStatus.OK);
    }

    @Operation(summary = "create a new virtual machine model")
    @PostMapping({"", "/"})
    @ResponseStatus(HttpStatus.CREATED)
    ResponseEntity<VirtualMachineModelDTO> addVirtualMachineModel(@RequestBody @Valid VirtualMachineModelDTO virtualMachineModelDTO) {
        String courseId = virtualMachineModelDTO.getCourseId();
        VirtualMachineModelDTO virtualMachineModel = virtualMachineService.createVirtualMachineModel(courseId, virtualMachineModelDTO);
        return new ResponseEntity<>(ModelHelper.enrich(virtualMachineModel, courseId),HttpStatus.OK);
    }

    @Operation(summary = "delete an existing virtual machine model")
    @DeleteMapping("/{modelId}")
    void deleteVirtualMachineModel(@PathVariable @NotNull Long modelId) {
        if (!virtualMachineService.deleteVirtualMachineModel(modelId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "please turn off all the virtual machines using this model");
        }
    }

    @Operation(summary = "get all system images")
    @GetMapping({"", "/"})
    List<SystemImage> all() {
        return virtualMachineService.getImages();
    }
}
