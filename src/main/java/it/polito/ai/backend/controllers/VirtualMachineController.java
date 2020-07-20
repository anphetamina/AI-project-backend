package it.polito.ai.backend.controllers;

import it.polito.ai.backend.dtos.*;
import it.polito.ai.backend.services.team.TeamNotFoundException;
import it.polito.ai.backend.services.team.TeamService;
import it.polito.ai.backend.services.team.TeamServiceConflictException;
import it.polito.ai.backend.services.team.TeamServiceNotFoundException;
import it.polito.ai.backend.services.vm.*;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/API/virtual-machines")
public class VirtualMachineController {

    @Autowired
    VirtualMachineService virtualMachineService;
    @Autowired
    TeamService teamService;
    @Autowired
    ModelMapper modelMapper;

    @GetMapping("/{id}")
    VirtualMachineDTO getOne(@PathVariable @NotNull Long id) {
        try {
            VirtualMachineDTO virtualMachineDTO = virtualMachineService.getVirtualMachine(id).orElseThrow(() -> new VirtualMachineServiceNotFoundException(id.toString()));
            Long teamId = virtualMachineService.getTeamForVirtualMachine(id).map(TeamDTO::getId).orElse(null);
            return ModelHelper.enrich(virtualMachineDTO, teamId);
        } catch (VirtualMachineNotFoundException | TeamServiceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (VirtualMachineServiceConflictException | TeamServiceConflictException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/virtual-machines/{vmId}/owners")
    CollectionModel<StudentDTO> getOwners(@PathVariable @NotNull Long vmId) {
        try {
            List<StudentDTO> studentDTOList = virtualMachineService.getOwnersForVirtualMachine(vmId).stream().map(ModelHelper::enrich).collect(Collectors.toList());
            Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(VirtualMachineController.class).getOwners(vmId)).withSelfRel();
            return CollectionModel.of(studentDTOList, selfLink);
        } catch (VirtualMachineNotFoundException | TeamServiceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (VirtualMachineServiceConflictException | TeamServiceConflictException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/virtual-machines/{vmId}/model")
    VirtualMachineModelDTO getModel(@PathVariable @NotNull Long vmId) {
        try {
            VirtualMachineModelDTO virtualMachineModelDTO = virtualMachineService.getVirtualMachineModelForVirtualMachine(vmId).orElseThrow(() -> new VirtualMachineModelNotDefinedException(vmId.toString()));
            String courseName = virtualMachineService.getCourseForVirtualMachineModel(virtualMachineModelDTO.getId()).map(CourseDTO::getName).orElse(null);
            return ModelHelper.enrich(virtualMachineModelDTO, courseName);
        } catch (VirtualMachineNotFoundException | TeamServiceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (VirtualMachineServiceConflictException | TeamServiceConflictException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/virtual-machines/{vmId}/team")
    TeamDTO getTeam(@PathVariable @NotNull Long vmId) {
        try {
            TeamDTO teamDTO = virtualMachineService.getTeamForVirtualMachine(vmId).orElseThrow(() -> new TeamNotFoundException(vmId.toString()));
            String courseName = teamService.getCourse(teamDTO.getId()).map(CourseDTO::getName).orElse(null);
            return ModelHelper.enrich(teamDTO, courseName);
        } catch (VirtualMachineNotFoundException | TeamServiceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (VirtualMachineServiceConflictException | TeamServiceConflictException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping({"", "/"})
    @ResponseStatus(HttpStatus.CREATED)
    VirtualMachineDTO addVirtualMachine(@RequestBody @NotNull Map<String, Object> map) {

        if (!(map.containsKey("studentId") && map.containsKey("teamId") && map.containsKey("numVcpu") && map.containsKey("diskSpace") && map.containsKey("ram"))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid number of parameters");
        } else {

            try {

                String studentId = modelMapper.map(map.get("studentId"), String.class);
                Long teamId = modelMapper.map(map.get("teamId"), Long.class);
                int numVcpu = modelMapper.map(map.get("numVcpu"), int.class);
                int diskSpace = modelMapper.map(map.get("diskSpace"), int.class);
                int ram = modelMapper.map(map.get("ram"), int.class);

                /**
                 * parameters validation
                 */

                if (studentId.isEmpty() || studentId.trim().isEmpty()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid student id" + studentId);
                }
                if (teamId == null || teamId <= 0) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid team id" + teamId);
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

                VirtualMachineDTO virtualMachine = virtualMachineService.createVirtualMachine(studentId, teamId, numVcpu, diskSpace, ram);
                return ModelHelper.enrich(virtualMachine, teamId);
            } catch (VirtualMachineNotFoundException | TeamServiceNotFoundException e) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
            } catch (VirtualMachineServiceConflictException | TeamServiceConflictException e) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
            } catch (ResponseStatusException e) {
                throw e;
            } catch (Exception e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }


    }

    @PutMapping({"/{id}"})
    VirtualMachineDTO setVirtualMachine(@PathVariable @NotNull Long id, @RequestBody @Valid VirtualMachineDTO virtualMachineDTO) {
        try {
            VirtualMachineDTO newVirtualMachine = virtualMachineService.updateVirtualMachine(id, virtualMachineDTO);
            Long teamId = virtualMachineService.getTeamForVirtualMachine(id).map(TeamDTO::getId).orElse(null);
            return ModelHelper.enrich(newVirtualMachine, teamId);
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
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @PutMapping("/{id}/on")
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

    @PutMapping("/{id}/off")
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

    @PostMapping("/{id}/owners")
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
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



}
