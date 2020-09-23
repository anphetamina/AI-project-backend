package it.polito.ai.backend.controllers;

import io.swagger.v3.oas.annotations.Operation;
import it.polito.ai.backend.dtos.*;
import it.polito.ai.backend.entities.Student;
import it.polito.ai.backend.services.notification.NotificationService;
import it.polito.ai.backend.services.team.TeamNotFoundException;
import it.polito.ai.backend.services.team.TeamService;
import it.polito.ai.backend.services.vm.VirtualMachineService;
import net.minidev.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/API/teams")
@Validated
public class TeamController {

    @Autowired
    TeamService teamService;
    @Autowired
    VirtualMachineService virtualMachineService;
    @Autowired
    NotificationService notificationService;

    @Operation(summary = "get team")
    @GetMapping("/{teamId}")
    ResponseEntity<TeamDTO> getOne(@PathVariable @NotNull Long teamId) {
        TeamDTO teamDTO = teamService.getTeam(teamId).orElseThrow(() -> new TeamNotFoundException(teamId.toString()));
        String courseId = teamService.getCourseForTeam(teamId).map(CourseDTO::getId).orElse(null);
        Long configurationId = virtualMachineService.getConfigurationForTeam(teamId).map(ConfigurationDTO::getId).orElse(null);
        return new  ResponseEntity<>(ModelHelper.enrich(teamDTO, courseId, configurationId),HttpStatus.OK);
    }

    @Operation(summary = "get team members")
    @GetMapping("/{teamId}/members")
    ResponseEntity<CollectionModel<StudentDTO>> getMembers(@PathVariable @NotNull Long teamId) {
        List<StudentDTO> students = teamService.getMembers(teamId).stream()
                .map(studentDTO ->
                {
                    return ModelHelper.enrich(studentDTO,null);
                }).collect(Collectors.toList());
        Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(TeamController.class).getMembers(teamId)).withSelfRel();
        return new ResponseEntity<>(CollectionModel.of(students, selfLink),HttpStatus.OK);
    }


    @Operation(summary = "get team members status only if in db there are present tokens. The expired token are cancel automaticaly at 4am evry day")
    @GetMapping("/{teamId}/members/status-list")
    ResponseEntity<List<JSONObject>> getMembersStatus(@PathVariable Long teamId) {

        List<JSONObject> listMembers = new ArrayList<>();
        Map<StudentDTO,String> memberAndStatus = new HashMap<>();
        List<StudentDTO> students = teamService.getMembers(teamId).stream()
                .map(studentDTO -> {
                    return ModelHelper.enrich(studentDTO,null);
                }).collect(Collectors.toList());
        List<TokenDTO>  tokenDTOS = notificationService.getTokenTeam(teamId);
        if(tokenDTOS.isEmpty())
            throw new TeamNotFoundException("Non exist propose for team: "+teamId);
        students.forEach(studentDTO -> {
            for (TokenDTO tokenDTO:tokenDTOS) {
                if(tokenDTO.getStudentId().equals(studentDTO.getId())) 
                    memberAndStatus.put(studentDTO,tokenDTO.getStatus().toString());
            }
            if(tokenDTOS.stream().noneMatch(tokenDTO -> tokenDTO.getStudentId().equals(studentDTO.getId()))){
                memberAndStatus.put(studentDTO,"PROPONENT");
            }


        });

        memberAndStatus.forEach( (k, v) ->{
            JSONObject entity = new JSONObject();
            entity.put("student" , k);
            entity.put("status",v);
            listMembers.add(entity);
        });
        return new ResponseEntity<>(listMembers,HttpStatus.OK);

    }

    @Operation(summary = "get virtual machines for a team")
    @GetMapping("/{teamId}/virtual-machines")
    ResponseEntity<CollectionModel<VirtualMachineDTO>> getVirtualMachines(@PathVariable @NotNull Long teamId) {
        List<VirtualMachineDTO> virtualMachineDTOList = virtualMachineService.getVirtualMachinesForTeam(teamId);
        Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(TeamController.class).getVirtualMachines(teamId)).withSelfRel();
        return new ResponseEntity<>(CollectionModel.of(virtualMachineDTOList, selfLink),HttpStatus.OK);
    }

    @Operation(summary = "get the active number of cpu cores by a team")
    @GetMapping("/{teamId}/virtual-machines/active-cpu")
    int getActiveVcpu(@PathVariable @NotNull Long teamId) {
        return virtualMachineService.getActiveVcpuForTeam(teamId);
    }

    @Operation(summary = "get the active number of disk amount (MB) by a team")
    @GetMapping("/{teamId}/virtual-machines/active-disk-space")
    int getActiveDiskSpace(@PathVariable @NotNull Long teamId) {
        return virtualMachineService.getActiveDiskSpaceForTeam(teamId);
    }

    @Operation(summary = "get the active number of ram (GB) by a team")
    @GetMapping("/{teamId}/virtual-machines/active-ram")
    int getActiveRam(@PathVariable @NotNull Long teamId) {
        return virtualMachineService.getActiveRAMForTeam(teamId);
    }

    @Operation(summary = "get the total number of virtual machines by a team")
    @GetMapping("/{teamId}/virtual-machines/tot")
    int getCountVirtualMachines(@PathVariable @NotNull Long teamId) {
        return virtualMachineService.getCountVirtualMachinesForTeam(teamId);
    }

    @Operation(summary = "get the active number of virtual machines by a team")
    @GetMapping("/{teamId}/virtual-machines/tot-on")
    int getCountActiveVirtualMachines(@PathVariable @NotNull Long teamId) {
        return virtualMachineService.getCountActiveVirtualMachinesForTeam(teamId);
    }

    @Operation(summary = "get the active resources and the configuration max numbers by a team")
    @GetMapping("/{teamId}/virtual-machines/resources")
    ResponseEntity<ResourcesResponse> getResources(@PathVariable @NotNull Long teamId) {
        return new ResponseEntity<>(virtualMachineService.getResourcesByTeam(teamId),HttpStatus.OK);
    }
}
