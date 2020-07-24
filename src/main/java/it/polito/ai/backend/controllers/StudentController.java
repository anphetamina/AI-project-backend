package it.polito.ai.backend.controllers;

import it.polito.ai.backend.dtos.*;
import it.polito.ai.backend.services.exercise.ExerciseNotFoundException;
import it.polito.ai.backend.services.exercise.ExerciseService;
import it.polito.ai.backend.dtos.CourseDTO;
import it.polito.ai.backend.dtos.StudentDTO;
import it.polito.ai.backend.dtos.TeamDTO;
import it.polito.ai.backend.dtos.VirtualMachineDTO;
import it.polito.ai.backend.services.exercise.ExerciseServiceException;
import it.polito.ai.backend.services.notification.NotificationService;
import it.polito.ai.backend.services.team.*;
import it.polito.ai.backend.services.vm.VirtualMachineNotFoundException;
import it.polito.ai.backend.services.vm.VirtualMachineService;
import it.polito.ai.backend.services.vm.VirtualMachineServiceConflictException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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

    @GetMapping({"", "/"})
    CollectionModel<StudentDTO> all() {
        List<StudentDTO> students = teamService.getAllStudents().stream().map(ModelHelper::enrich).collect(Collectors.toList());
        Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(StudentController.class).all()).withSelfRel();
        return CollectionModel.of(students, selfLink);
    }

    @GetMapping("/{studentId}")
    StudentDTO getOne(@PathVariable @NotBlank String studentId) {
        return ModelHelper.enrich(teamService.getStudent(studentId).orElseThrow(() -> new StudentNotFoundException(studentId)));
    }

    @GetMapping("/{studentId}/courses")
    CollectionModel<CourseDTO> getCourses(@PathVariable @NotBlank String studentId) {
        List<CourseDTO> courses = teamService.getCourses(studentId).stream().map(ModelHelper::enrich).collect(Collectors.toList());
        Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(StudentController.class).getCourses(studentId)).withSelfRel();
        return CollectionModel.of(courses, selfLink);
    }

    @GetMapping("/{studentId}/teams")
    CollectionModel<TeamDTO> getTeams(@PathVariable @NotBlank String studentId) {
        List<TeamDTO> teams = teamService.getTeamsForStudent(studentId).stream()
                .map(t -> {
                    String courseId = teamService.getCourseForTeam(t.getId()).map(CourseDTO::getId).orElseThrow(() -> new CourseNotFoundException(String.format("for team %s", t.getId())));
                    return ModelHelper.enrich(t, courseId);
                })
                .collect(Collectors.toList());
        Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(StudentController.class).getTeams(studentId)).withSelfRel();
        return CollectionModel.of(teams, selfLink);
    }

    @PostMapping({"", "/"})
    StudentDTO addStudent(@RequestBody @Valid StudentDTO studentDTO) {
        if (!teamService.addStudent(studentDTO)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, String.format("student %s already exists", studentDTO.getId()));
        }
        return ModelHelper.enrich(studentDTO);
    }

    @GetMapping("/{studentId}/assignments")
    List<AssignmentDTO> getAssignments(@PathVariable @NotBlank String studentId){
        Optional<StudentDTO> studentDTO = teamService.getStudent(studentId);
        if(!studentDTO.isPresent())
            throw new StudentNotFoundException(studentId);
        List<AssignmentDTO> assignmentDTOS = exerciseService.getAssignmentsForStudent(studentId);
        List<AssignmentDTO> assignmentDTOList = new ArrayList<>();
        for (AssignmentDTO a:assignmentDTOS) {
            Long exerciseId = exerciseService.getExerciseForAssignment(a.getId()).map(ExerciseDTO::getId).orElseThrow( () -> new ExerciseNotFoundException(a.getId().toString()));
            String courseId = exerciseService.getCourse(exerciseId).map(CourseDTO::getId).orElseThrow(() -> new CourseNotFoundException(exerciseId.toString()));
            assignmentDTOList.add(ModelHelper.enrich(a,studentId,exerciseId,courseId));

        }
        return assignmentDTOList;

    }
}
