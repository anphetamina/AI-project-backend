package it.polito.ai.backend.controllers;

import io.swagger.v3.oas.annotations.Operation;
import it.polito.ai.backend.dtos.*;
import it.polito.ai.backend.services.assignment.AssignmentService;
import it.polito.ai.backend.services.notification.NotificationService;
import it.polito.ai.backend.services.team.*;
import it.polito.ai.backend.services.team.StudentNotFoundException;
import it.polito.ai.backend.services.team.TeamService;
import it.polito.ai.backend.services.vm.VirtualMachineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/API/students")
@Validated
public class StudentController {

    @Autowired
    TeamService teamService;
    @Autowired
    AssignmentService assignmentService;
    @Autowired
    NotificationService notificationService;
    @Autowired
    VirtualMachineService virtualMachineService;

    @Operation(summary = "get all students")
    @GetMapping({"", "/"})
    ResponseEntity<CollectionModel<StudentDTO>> all() {
        List<StudentDTO> students = teamService.getAllStudents().stream().map(ModelHelper::enrich).collect(Collectors.toList());
        Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(StudentController.class).all()).withSelfRel();
        return new ResponseEntity<>(CollectionModel.of(students, selfLink), HttpStatus.OK);
    }

    @Operation(summary = "get student")
    @GetMapping("/{studentId}")
    ResponseEntity<StudentDTO> getOne(@PathVariable @NotBlank String studentId) {
        return new ResponseEntity<>(ModelHelper.enrich(teamService.getStudent(studentId)
                .orElseThrow(() -> new StudentNotFoundException(studentId))),HttpStatus.OK);
    }

    @Operation(summary = "get courses to which a student is enrolled")
    @GetMapping("/{studentId}/courses")
    ResponseEntity<CollectionModel<CourseDTO>> getCourses(@PathVariable @NotBlank String studentId) {
        List<CourseDTO> courses = teamService.getCourses(studentId)
                .stream()
                .map(c -> {
                    Long modelId = virtualMachineService.getVirtualMachineModelForCourse(c.getId()).map(VirtualMachineModelDTO::getId).orElse(null);
                    return ModelHelper.enrich(c, modelId);
                })
                .collect(Collectors.toList());
        Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(StudentController.class).getCourses(studentId)).withSelfRel();
        return new ResponseEntity<>(CollectionModel.of(courses, selfLink),HttpStatus.OK);
    }



    @Operation(summary = "get team of which a student is part of in defined course")
    @GetMapping("/{studentId}/courses/{courseId}/team")
    ResponseEntity<TeamDTO> getTeamForStudentAndCourse(@PathVariable String studentId, @PathVariable String courseId) {
        TeamDTO teamDTO = teamService.getTeamForStudentAndCourse(studentId, courseId)
                    .orElseThrow(() ->
                            new TeamNotFoundException("Not exist a team active for student " + studentId + " enrolld to course: " + courseId));
            Long configurationId = virtualMachineService.getConfigurationForTeam(teamDTO.getId()).map(ConfigurationDTO::getId).orElse(null);
            return new ResponseEntity<>( ModelHelper
                    .enrich(teamDTO, courseId,configurationId),HttpStatus.OK);

    }

    @Operation(summary = "get unconfirmed teams of which a student is part of in defined course")
    @GetMapping("/{studentId}/courses/{courseId}/unconfirmed-team")
    ResponseEntity<CollectionModel<TeamDTO>>getProposeTeamsForStudentAndCourse(@PathVariable String studentId, @PathVariable String courseId) {
        List<TeamDTO> teams = teamService.getProposeTeamsForStudentAndCourse(studentId, courseId)
                    .stream()
                    .map(t -> ModelHelper.enrich(t, courseId,null))
                    .collect(Collectors.toList());
            Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(StudentController.class).getProposeTeamsForStudentAndCourse(studentId, courseId)).withSelfRel();
            return new ResponseEntity<>(CollectionModel.of(teams, selfLink),HttpStatus.OK);
    }




    @Operation(summary = "get the papers of a student")
    @GetMapping("/{studentId}/assignments/{assignmentId}/papers")
    ResponseEntity<CollectionModel<PaperDTO>> getPapers(@PathVariable @NotBlank String studentId, @PathVariable @NotNull Long assignmentId ) {
        List<PaperDTO> paperDTOS =  assignmentService.getPaperByStudentAndAssignment(studentId,assignmentId).stream()
                .map(a -> ModelHelper.enrich(a,studentId,assignmentId))
                .collect(Collectors.toList());
        Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(StudentController.class).getPapers(studentId,assignmentId)).withSelfRel();
        return new ResponseEntity<>(CollectionModel.of(paperDTOS, selfLink),HttpStatus.OK);

    }



    @Operation(summary = "get the owned virtual machines by a student")
    @GetMapping("/{studentId}/virtual-machines")
    ResponseEntity<CollectionModel<VirtualMachineDTO>> getVirtualMachines (@PathVariable @NotBlank String studentId){
        List<VirtualMachineDTO> virtualMachineDTOList = virtualMachineService.getVirtualMachinesForStudent(studentId)
                .stream()
                .map(vm -> {
                    Long teamId = virtualMachineService.getTeamForVirtualMachine(vm.getId()).map(TeamDTO::getId).orElse(null);
                    Long modelId = virtualMachineService.getVirtualMachineModelForVirtualMachine(vm.getId()).map(VirtualMachineModelDTO::getId).orElse(null);
                    return ModelHelper.enrich(vm, teamId, modelId);
                })
                .collect(Collectors.toList());
        Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(StudentController.class).getVirtualMachines(studentId)).withSelfRel();
        return new ResponseEntity<>(CollectionModel.of(virtualMachineDTOList, selfLink),HttpStatus.OK);
    }
}

