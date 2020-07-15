package it.polito.ai.backend.controllers;

import it.polito.ai.backend.dtos.VirtualMachineConfigurationDTO;
import it.polito.ai.backend.dtos.VirtualMachineDTO;
import it.polito.ai.backend.services.team.TeamServiceConflictException;
import it.polito.ai.backend.services.team.TeamServiceNotFoundException;
import it.polito.ai.backend.services.vm.*;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Map;

@RestController
@RequestMapping("/API/virtual-machines")
public class VirtualMachineController {

    @Autowired
    VirtualMachineService virtualMachineService;
    @Autowired
    ModelMapper modelMapper;

    @PostMapping({"", "/"})
    @ResponseStatus(HttpStatus.CREATED)
    VirtualMachineDTO addVirtualMachine(@RequestBody @NotNull Map<String, Object> map) {

        if (!(map.containsKey("studentId") && map.containsKey("teamId") && map.containsKey("configurationId") && map.containsKey("courseName") && map.containsKey("modelId") && map.containsKey("numVcpu") && map.containsKey("diskSpace") && map.containsKey("ram"))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid number of parameters");
        } else {

            try {

                String studentId = modelMapper.map(map.get("studentId"), String.class);
                Long teamId = modelMapper.map(map.get("teamId"), Long.class);
                Long configurationId = modelMapper.map(map.get("configurationId"), Long.class);
                String courseName = modelMapper.map(map.get("courseName"), String.class);
                Long modelId = modelMapper.map(map.get("modelId"), Long.class);
                int numVcpu = modelMapper.map(map.get("numVcpu"), int.class);
                int diskSpace = modelMapper.map(map.get("diskSpace"), int.class);
                int ram = modelMapper.map(map.get("ram"), int.class);

                /**
                 * parameters validation
                 */

                if (studentId.isEmpty() || studentId.trim().isEmpty()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid student id" + studentId);
                }
                if (courseName.isEmpty() || courseName.trim().isEmpty()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid course name" + courseName);
                }
                if (teamId == null || teamId <= 0) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid team id" + teamId);
                }
                if (configurationId == null || configurationId <= 0) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid configuration id" + configurationId);
                }
                if (modelId == null || modelId <= 0) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid model id" + modelId);
                }
                if (numVcpu <= 0) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid num vcpu" + numVcpu);
                }
                if (diskSpace <= 0) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid disk space" + diskSpace);
                }
                if (ram <= 0) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid ram" + ram);
                }

                VirtualMachineDTO virtualMachine = virtualMachineService.createVirtualMachine(studentId, teamId, configurationId, courseName, modelId, numVcpu, diskSpace, ram);
                return ModelHelper.enrich(virtualMachine);
            } catch (VirtualMachineNotFoundException | TeamServiceNotFoundException e) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
            } catch (VirtualMachineServiceConflictException | TeamServiceConflictException e) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
            } catch (Exception e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }


    }

    @PutMapping({"/{id}"})
    VirtualMachineDTO setVirtualMachine(@PathVariable @NotNull Long id, @RequestBody @Valid VirtualMachineDTO virtualMachineDTO) {
        try {
            VirtualMachineDTO newVirtualMachine = virtualMachineService.updateVirtualMachine(id, virtualMachineDTO);
            return ModelHelper.enrich(newVirtualMachine);
        } catch (VirtualMachineNotFoundException | TeamServiceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (VirtualMachineServiceConflictException | TeamServiceConflictException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping({"", "/"})
    void deleteVirtualMachine(@RequestBody @NotNull Long id) {
        try {
            if (!virtualMachineService.deleteVirtualMachine(id)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT);
            }
        } catch (VirtualMachineNotFoundException | TeamServiceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (VirtualMachineServiceConflictException | TeamServiceConflictException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @PutMapping("/{id}/turnOn")
    @ResponseStatus(HttpStatus.OK)
    void turnOn(@PathVariable @NotNull Long id) {
        try {
            virtualMachineService.turnOnVirtualMachine(id);
        } catch (VirtualMachineNotFoundException | TeamServiceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (VirtualMachineServiceConflictException | TeamServiceConflictException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}/turnOff")
    @ResponseStatus(HttpStatus.OK)
    void turnOff(@PathVariable @NotNull Long id) {
        try {
            virtualMachineService.turnOffVirtualMachine(id);
        } catch (VirtualMachineNotFoundException | TeamServiceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (VirtualMachineServiceConflictException | TeamServiceConflictException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/{id}/addOwner")
    @ResponseStatus(HttpStatus.OK)
    void shareOwnership(@PathVariable @NotNull Long id, @RequestBody @NotBlank String studentId) {
        try {
            if (!virtualMachineService.addOwnerToVirtualMachine(studentId, id)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot share ownership with "+studentId);
            }
        } catch (VirtualMachineNotFoundException | TeamServiceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (VirtualMachineServiceConflictException | TeamServiceConflictException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/{id}/removeOwner")
    @ResponseStatus(HttpStatus.OK)
    void removeOwnership(@PathVariable @NotNull Long id, @RequestBody @NotBlank String studentId) {
        try {
            if (!virtualMachineService.removeOwnerFromVirtualMachine(studentId, id)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot remove " + studentId + " from the owners");
            }
        } catch (VirtualMachineNotFoundException | TeamServiceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (VirtualMachineServiceConflictException | TeamServiceConflictException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    VirtualMachineDTO getOne(@PathVariable @NotNull Long id) {
        try {
            return ModelHelper.enrich(virtualMachineService.getVirtualMachine(id).orElseThrow(() -> new VirtualMachineServiceNotFoundException(id.toString())));
        } catch (VirtualMachineNotFoundException | TeamServiceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (VirtualMachineServiceConflictException | TeamServiceConflictException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
