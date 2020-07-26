package it.polito.ai.backend.controllers;

import it.polito.ai.backend.dtos.*;
import it.polito.ai.backend.services.team.TeamNotFoundException;
import it.polito.ai.backend.services.team.TeamService;
import it.polito.ai.backend.services.vm.VirtualMachineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/API/teams")
@Validated
public class TeamController {

    // todo move createTeam from course controller

    @Autowired
    TeamService teamService;
    @Autowired
    VirtualMachineService virtualMachineService;

    @GetMapping("/{teamId}")
    TeamDTO getOne(@PathVariable @NotNull Long teamId) {
        TeamDTO teamDTO = teamService.getTeam(teamId).orElseThrow(() -> new TeamNotFoundException(teamId.toString()));
        String courseId = teamService.getCourseForTeam(teamId).map(CourseDTO::getId).orElse(null);
        Long configurationId = virtualMachineService.getConfigurationForTeam(teamId).map(ConfigurationDTO::getId).orElse(null);
        return ModelHelper.enrich(teamDTO, courseId, configurationId);
    }

    @GetMapping("/{teamId}/members")
    CollectionModel<StudentDTO> getMembers(@PathVariable @NotNull Long teamId) {
        List<StudentDTO> students = teamService.getMembers(teamId).stream().map(ModelHelper::enrich).collect(Collectors.toList());
        Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(TeamController.class).getMembers(teamId)).withSelfRel();
        return CollectionModel.of(students, selfLink);
    }

    @GetMapping("/{teamId}/virtual-machines")
    CollectionModel<VirtualMachineDTO> getVirtualMachines(@PathVariable @NotNull Long teamId) {
        List<VirtualMachineDTO> virtualMachineDTOList = virtualMachineService.getVirtualMachinesForTeam(teamId);
        Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(TeamController.class).getVirtualMachines(teamId)).withSelfRel();
        return CollectionModel.of(virtualMachineDTOList, selfLink);
    }

    @GetMapping("/{teamId}/virtual-machines/active-cpu")
    int getActiveVcpu(@PathVariable @NotNull Long teamId) {
        return virtualMachineService.getActiveVcpuForTeam(teamId);
    }

    @GetMapping("/{teamId}/virtual-machines/active-disk-space")
    int getActiveDiskSpace(@PathVariable @NotNull Long teamId) {
        return virtualMachineService.getActiveDiskSpaceForTeam(teamId);
    }

    @GetMapping("/{teamId}/virtual-machines/active-ram")
    int getActiveRam(@PathVariable @NotNull Long teamId) {
        return virtualMachineService.getActiveRAMForTeam(teamId);
    }

    @GetMapping("/{teamId}/virtual-machines/tot")
    int getCountVirtualMachines(@PathVariable @NotNull Long teamId) {
        return virtualMachineService.getCountVirtualMachinesForTeam(teamId);
    }

    @GetMapping("/{teamId}/virtual-machines/tot-on")
    int getCountActiveVirtualMachines(@PathVariable @NotNull Long teamId) {
        return virtualMachineService.getCountActiveVirtualMachinesForTeam(teamId);
    }

    @GetMapping("/{teamId}/virtual-machines/resources")
    Map<String, Integer> getResources(@PathVariable @NotNull Long teamId) {
        return virtualMachineService.getResourcesByTeam(teamId);
    }
}
