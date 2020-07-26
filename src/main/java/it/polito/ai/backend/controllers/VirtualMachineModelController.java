package it.polito.ai.backend.controllers;

import it.polito.ai.backend.dtos.CourseDTO;
import it.polito.ai.backend.dtos.VirtualMachineModelDTO;
import it.polito.ai.backend.services.vm.VirtualMachineModelNotFoundException;
import it.polito.ai.backend.services.vm.VirtualMachineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@RestController
@RequestMapping("/API/virtual-machine-models")
@Validated
public class VirtualMachineModelController {

    @Autowired
    VirtualMachineService virtualMachineService;

    @GetMapping("/{modelId}")
    VirtualMachineModelDTO getOne(@PathVariable @NotNull Long modelId) {
        VirtualMachineModelDTO virtualMachineModelDTO = virtualMachineService.getVirtualMachineModel(modelId)
                .orElseThrow(() -> new VirtualMachineModelNotFoundException(modelId.toString()));
        String courseId = virtualMachineService.getCourseForVirtualMachineModel(modelId).map(CourseDTO::getId).orElse(null);
        return ModelHelper.enrich(virtualMachineModelDTO, courseId);
    }

    @PostMapping({"", "/"})
    @ResponseStatus(HttpStatus.CREATED)
    VirtualMachineModelDTO addVirtualMachineModel(@RequestBody @Valid VirtualMachineModelDTO model) {
        // todo
        String courseId = "";
        VirtualMachineModelDTO virtualMachineModel = virtualMachineService.createVirtualMachineModel(courseId, model);
        return ModelHelper.enrich(virtualMachineModel, courseId);
    }

    @DeleteMapping("/{modelId}")
    void deleteVirtualMachineModel(@PathVariable @NotNull Long modelId) {
        if (!virtualMachineService.deleteVirtualMachineModel(modelId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "please turn off all the virtual machines using this model");
        }
    }
}
