package it.polito.ai.backend.controllers;

import io.swagger.v3.oas.annotations.Operation;
import it.polito.ai.backend.dtos.*;
import it.polito.ai.backend.entities.Team;
import it.polito.ai.backend.entities.TeamStatus;
import it.polito.ai.backend.services.exercise.ExerciseNotFoundException;
import it.polito.ai.backend.services.exercise.ExerciseService;
import it.polito.ai.backend.services.notification.NotificationService;
import it.polito.ai.backend.services.notification.TokenExpiredException;
import it.polito.ai.backend.services.team.*;
import it.polito.ai.backend.services.vm.VirtualMachineNotFoundException;
import it.polito.ai.backend.services.team.StudentNotFoundException;
import it.polito.ai.backend.services.team.TeamService;
import it.polito.ai.backend.services.vm.VirtualMachineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/API/students")
@Validated
public class StudentController {

    @Autowired
    TeamService teamService;
    @Autowired
    ExerciseService exerciseService;
    @Autowired
    NotificationService notificationService;
    @Autowired
    VirtualMachineService virtualMachineService;

    @Operation(summary = "get all students")
    @GetMapping({"", "/"})
    CollectionModel<StudentDTO> all() {
        List<StudentDTO> students = teamService.getAllStudents().stream().map(ModelHelper::enrich).collect(Collectors.toList());
        Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(StudentController.class).all()).withSelfRel();
        return CollectionModel.of(students, selfLink);
    }

    @Operation(summary = "get student")
    @GetMapping("/{studentId}")
    StudentDTO getOne(@PathVariable @NotBlank String studentId) {
        return ModelHelper.enrich(teamService.getStudent(studentId).orElseThrow(() -> new StudentNotFoundException(studentId)));
    }

    @Operation(summary = "get courses to which a student is enrolled")
    @GetMapping("/{studentId}/courses")
    CollectionModel<CourseDTO> getCourses(@PathVariable @NotBlank String studentId) {
        List<CourseDTO> courses = teamService.getCourses(studentId)
                .stream()
                .map(c -> {
                    Long modelId = virtualMachineService.getVirtualMachineModelForCourse(c.getId()).map(VirtualMachineModelDTO::getId).orElse(null);
                    return ModelHelper.enrich(c, modelId);
                })
                .collect(Collectors.toList());
        Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(StudentController.class).getCourses(studentId)).withSelfRel();
        return CollectionModel.of(courses, selfLink);
    }

    @Operation(summary = "get teams of which a student is part of")
    @GetMapping("/{studentId}/teams")
    CollectionModel<TeamDTO> getTeams(@PathVariable @NotBlank String studentId) {
        List<TeamDTO> teams = teamService.getTeamsForStudent(studentId).stream()
                .map(t -> {
                    String courseId = teamService.getCourseForTeam(t.getId()).map(CourseDTO::getId).orElse(null);
                    Long configurationId = virtualMachineService.getConfigurationForTeam(t.getId()).map(ConfigurationDTO::getId).orElse(null);
                    return ModelHelper.enrich(t, courseId, configurationId);
                })
                .collect(Collectors.toList());
        Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(StudentController.class).getTeams(studentId)).withSelfRel();
        return CollectionModel.of(teams, selfLink);
    }

    @GetMapping("/{studentId}/courses/{courseId}/team")
    TeamDTO getTeamForStudentAndCourse(@PathVariable String studentId, @PathVariable String courseId) {
        try {

            TeamDTO teamDTO = teamService.getTeamForStudentAndCourse(studentId, courseId)
                    .orElseThrow(() ->
                            new TeamNotFoundException("Not exist a team active for student " + studentId + " enrolld to course: " + courseId));
            Long configurationId = virtualMachineService.getConfigurationForTeam(teamDTO.getId()).map(ConfigurationDTO::getId).orElse(null);
            return ModelHelper
                    .enrich(teamDTO, courseId,configurationId);
        }/* catch (AccessDeniedException exception) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, exception.getMessage());
        }*/ catch (TeamServiceException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage());
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
        }
    }

    @GetMapping("/{studentId}/courses/{courseId}/propose-team")
    CollectionModel<TeamDTO> getProposeTeamsForStudentAndCourse(@PathVariable String studentId, @PathVariable String courseId) {
        try {
            List<TeamDTO> teams = teamService.getProposeTeamsForStudentAndCourse(studentId, courseId)
                    .stream()
                    .map(t -> ModelHelper.enrich(t, courseId,null))
                    .collect(Collectors.toList());
            Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(StudentController.class).getProposeTeamsForStudentAndCourse(studentId, courseId)).withSelfRel();
            return CollectionModel.of(teams, selfLink);
        }/* catch (AccessDeniedException exception) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, exception.getMessage());
        }*/ catch (TeamServiceException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage());
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
        }
    }


    @Operation(summary = "create a new student")
    @PostMapping({"", "/"})
    StudentDTO addStudent(@RequestBody @Valid StudentDTO studentDTO) {
        if (!teamService.addStudent(studentDTO)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, String.format("student %s already exists", studentDTO.getId()));
        }
        return ModelHelper.enrich(studentDTO);
    }

    @Operation(summary = "get the assignments of a student")
    @GetMapping("/{studentId}/assignments")
    List<AssignmentDTO> getAssignments(@PathVariable @NotBlank String studentId) {
      /*  // todo collection model
        List<AssignmentDTO> assignmentDTOS = exerciseService.getAssignmentsForStudent(studentId).stream()
                .map(a -> {
                    Long exerciseId = exerciseService.getExerciseForAssignment(a.getId()).map(ExerciseDTO::getId).orElseThrow( () -> new ExerciseNotFoundException(a.getId().toString()));
                    return ModelHelper.enrich(a,studentId,exerciseId);
                })
                .collect(Collectors.toList());
        List<AssignmentDTO> assignmentDTOList = new ArrayList<>();
        for (AssignmentDTO a:assignmentDTOS) {
            Long exerciseId = exerciseService.getExerciseForAssignment(a.getId()).map(ExerciseDTO::getId).orElseThrow( () -> new ExerciseNotFoundException(a.getId().toString()));
            assignmentDTOList.add(ModelHelper.enrich(a,studentId,exerciseId));

        }
        return assignmentDTOList;*/
        return  new ArrayList<AssignmentDTO>();

    }

    @GetMapping("{studentId}/teams/confirm/{token}")
    boolean confirmToken(@PathVariable String token, @PathVariable String studentId) {
        try { //todo studentId è quello loggato
            return notificationService.confirm(token);
        } catch (Exception exception) {
            System.out.println(exception.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
        }
    }

    @GetMapping("{studentId}/teams/reject/{token}")
    boolean rejectToken(@PathVariable String token, @PathVariable String studentId) {
        try { //todo studentId è quello loggato
            return notificationService.reject(token);
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
        }
    }
        @Operation(summary = "get the owned virtual machines by a student")
        @GetMapping("/{studentId}/virtual-machines")
        CollectionModel<VirtualMachineDTO> getVirtualMachines (@PathVariable @NotBlank String studentId){
            List<VirtualMachineDTO> virtualMachineDTOList = virtualMachineService.getVirtualMachinesForStudent(studentId)
                    .stream()
                    .map(vm -> {
                        Long teamId = virtualMachineService.getTeamForVirtualMachine(vm.getId()).map(TeamDTO::getId).orElse(null);
                        Long modelId = virtualMachineService.getVirtualMachineModelForVirtualMachine(vm.getId()).map(VirtualMachineModelDTO::getId).orElse(null);
                        return ModelHelper.enrich(vm, teamId, modelId);
                    })
                    .collect(Collectors.toList());
            Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(StudentController.class).getVirtualMachines(studentId)).withSelfRel();
            return CollectionModel.of(virtualMachineDTOList, selfLink);
        }
}

