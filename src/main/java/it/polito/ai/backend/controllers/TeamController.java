package it.polito.ai.backend.controllers;

import it.polito.ai.backend.dtos.*;
import it.polito.ai.backend.entities.VirtualMachine;
import it.polito.ai.backend.services.team.*;
import it.polito.ai.backend.services.vm.ConfigurationNotDefinedException;
import it.polito.ai.backend.services.vm.VirtualMachineNotFoundException;
import it.polito.ai.backend.services.vm.VirtualMachineService;
import it.polito.ai.backend.services.vm.VirtualMachineServiceConflictException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/API/teams")
public class TeamController {

    @Autowired
    TeamService teamService;
    @Autowired
    VirtualMachineService virtualMachineService;
    @Autowired
    ModelMapper modelMapper;

    @GetMapping("/{teamId}")
    TeamDTO getOne(@PathVariable Long teamId) {
        try {
            TeamDTO teamDTO = teamService.getTeam(teamId).orElseThrow(() -> new TeamNotFoundException(teamId.toString()));
            String courseName = teamService.getCourse(teamId).map(CourseDTO::getName).orElse(null);
            return ModelHelper.enrich(teamDTO, courseName);
        }/* catch (AccessDeniedException exception) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, exception.getMessage());
        }*/ catch (TeamServiceException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage());
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
        }
    }

    @GetMapping("/{teamId}/members")
    CollectionModel<StudentDTO> getMembers(@PathVariable Long teamId) {
        try {
            List<StudentDTO> students = teamService.getMembers(teamId).stream().map(ModelHelper::enrich).collect(Collectors.toList());
            Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(TeamController.class).getMembers(teamId)).withSelfRel();
            return CollectionModel.of(students, selfLink);
        }/* catch (AccessDeniedException exception) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, exception.getMessage());
        }*/ catch (TeamServiceException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage());
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
        }
    }

    @PostMapping("/{teamId}/configuration")
    @ResponseStatus(HttpStatus.CREATED)
    VirtualMachineConfigurationDTO addConfiguration(@PathVariable @NotNull Long teamId, @RequestBody @NotNull Map<String, Object> map) {
        if (!(map.containsKey("min_vcpu") && map.containsKey("max_vcpu") && map.containsKey("min_disk_space") && map.containsKey("max_disk_space") && map.containsKey("min_ram") && map.containsKey("max_ram") && map.containsKey("max_on") && map.containsKey("tot"))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        } else {

            try {

                int min_vcpu = modelMapper.map(map.get("min_vcpu"), int.class);
                int max_vcpu = modelMapper.map(map.get("max_vcpu"), int.class);
                int min_disk_space = modelMapper.map(map.get("min_disk_space"), int.class);
                int max_disk_space = modelMapper.map(map.get("max_disk_space"), int.class);
                int min_ram = modelMapper.map(map.get("min_ram"), int.class);
                int max_ram = modelMapper.map(map.get("max_ram"), int.class);
                int max_on = modelMapper.map(map.get("max_on"), int.class);
                int tot = modelMapper.map(map.get("tot"), int.class);

                if (min_vcpu <= 0) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid min vcpu number " + min_vcpu);
                }
                if (max_vcpu <= 0) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid max vcpu number " + max_vcpu);
                }
                if (min_disk_space <= 0) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid min disk space number " + min_disk_space);
                }
                if (max_disk_space <= 0) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid max disk space number " + max_disk_space);
                }
                if (min_ram <= 0) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid min ram number " + min_ram);
                }
                if (max_ram <= 0) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid max ram number " + max_ram);
                }
                if (max_on <= 0) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid max on number " + max_on);
                }
                if (tot <= 0) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid tot number " + tot);
                }

                VirtualMachineConfigurationDTO virtualMachineConfiguration = virtualMachineService.createVirtualMachineConfiguration(teamId, min_vcpu, max_vcpu, min_disk_space, max_disk_space, min_ram, max_ram, max_on, tot);
                return ModelHelper.enrich(virtualMachineConfiguration, teamId);
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

    @PutMapping("{teamId}/configuration")
    VirtualMachineConfigurationDTO setConfiguration(@PathVariable @NotNull Long teamId, @RequestBody @Valid VirtualMachineConfigurationDTO configurationDTO) {
        try {
            VirtualMachineConfigurationDTO virtualMachineConfiguration = virtualMachineService.updateVirtualMachineConfiguration(teamId, configurationDTO);
            return ModelHelper.enrich(virtualMachineConfiguration, teamId);
        } catch (VirtualMachineNotFoundException | TeamServiceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (VirtualMachineServiceConflictException | TeamServiceConflictException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{teamId}/virtual-machines")
    CollectionModel<VirtualMachineDTO> getVirtualMachines(@PathVariable @NotNull Long teamId) {
        try {
            List<VirtualMachineDTO> virtualMachineDTOList = virtualMachineService.getVirtualMachinesForTeam(teamId);
            Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(TeamController.class).getVirtualMachines(teamId)).withSelfRel();
            return CollectionModel.of(virtualMachineDTOList, selfLink);
        } catch (VirtualMachineNotFoundException | TeamServiceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (VirtualMachineServiceConflictException | TeamServiceConflictException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{teamId}/configuration")
    VirtualMachineConfigurationDTO getConfiguration(@PathVariable @NotNull Long teamId) {
        try {
            return ModelHelper.enrich(virtualMachineService.getVirtualMachineConfigurationForTeam(teamId).orElseThrow(() -> new ConfigurationNotDefinedException(teamId.toString())), teamId);
        } catch (VirtualMachineNotFoundException | TeamServiceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (VirtualMachineServiceConflictException | TeamServiceConflictException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{teamId}/virtual-machines/active-cpu")
    int getActiveVcpu(@PathVariable @NotNull Long teamId) {
        try {
            return virtualMachineService.getActiveVcpuForTeam(teamId);
        } catch (VirtualMachineNotFoundException | TeamServiceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (VirtualMachineServiceConflictException | TeamServiceConflictException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{teamId}/virtual-machines/active-disk-space")
    int getActiveDiskSpace(@PathVariable @NotNull Long teamId) {
        try {
            return virtualMachineService.getActiveDiskSpaceForTeam(teamId);
        } catch (VirtualMachineNotFoundException | TeamServiceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (VirtualMachineServiceConflictException | TeamServiceConflictException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{teamId}/virtual-machines/active-ram")
    int getActiveRam(@PathVariable @NotNull Long teamId) {
        try {
            return virtualMachineService.getActiveRAMForTeam(teamId);
        } catch (VirtualMachineNotFoundException | TeamServiceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (VirtualMachineServiceConflictException | TeamServiceConflictException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{teamId}/virtual-machines/tot-on")
    int getCountActiveVirtualMachines(@PathVariable @NotNull Long teamId) {
        try {
            return virtualMachineService.getCountActiveVirtualMachinesForTeam(teamId);
        } catch (VirtualMachineNotFoundException | TeamServiceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (VirtualMachineServiceConflictException | TeamServiceConflictException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{teamId}/virtual-machines/resources")
    Map<String, Integer> getResources(@PathVariable @NotNull Long teamId) {
        try {
            return virtualMachineService.getResourcesByTeam(teamId);
        } catch (VirtualMachineNotFoundException | TeamServiceNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (VirtualMachineServiceConflictException | TeamServiceConflictException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
