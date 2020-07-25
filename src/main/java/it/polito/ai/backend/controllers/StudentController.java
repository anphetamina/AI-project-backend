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
import it.polito.ai.backend.services.notification.TokenExpiredException;
import it.polito.ai.backend.services.team.*;
import it.polito.ai.backend.services.vm.VirtualMachineNotFoundException;
import it.polito.ai.backend.services.vm.VirtualMachineService;
import it.polito.ai.backend.services.vm.VirtualMachineServiceConflictException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
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
        try {
            List<StudentDTO> students = teamService.getAllStudents().stream().map(ModelHelper::enrich).collect(Collectors.toList());
            Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(StudentController.class).all()).withSelfRel();
            return CollectionModel.of(students, selfLink);
        }/* catch (AccessDeniedException exception) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, exception.getMessage());
        }*/ catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
        }
    }

    @GetMapping("/{studentId}")
    StudentDTO getOne(@PathVariable String studentId) {
        try {
            return ModelHelper.enrich(teamService.getStudent(studentId).orElseThrow(() -> new StudentNotFoundException(studentId)));
        }/* catch (AccessDeniedException exception) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, exception.getMessage());
        }*/ catch (TeamServiceException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage());
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
        }
    }

    @GetMapping("/{studentId}/courses")
    CollectionModel<CourseDTO> getCourses(@PathVariable String studentId) {
        try {
            List<CourseDTO> courses = teamService.getCourses(studentId).stream().map(ModelHelper::enrich).collect(Collectors.toList());
            Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(StudentController.class).getCourses(studentId)).withSelfRel();
            return CollectionModel.of(courses, selfLink);
        }/* catch (AccessDeniedException exception) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, exception.getMessage());
        }*/ catch (TeamServiceException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage());
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
        }
    }

    @GetMapping("/{studentId}/teams")
    CollectionModel<TeamDTO> getTeams(@PathVariable String studentId) {
        try {
            List<TeamDTO> teams = teamService.getTeamsForStudent(studentId).stream()
                    .map(t -> {
                        String courseName = teamService.getCourse(t.getId()).map(CourseDTO::getId).orElse(null);
                        return ModelHelper.enrich(t, courseName);
                    })
                    .collect(Collectors.toList());
            Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(StudentController.class).getTeams(studentId)).withSelfRel();
            return CollectionModel.of(teams, selfLink);
        }/* catch (AccessDeniedException exception) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, exception.getMessage());
        }*/ catch (TeamServiceException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage());
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
        }
    }

    @PostMapping({"", "/"})
    StudentDTO addStudent(@RequestBody @Valid StudentDTO studentDTO) {
        try {
            if (!teamService.addStudent(studentDTO)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, studentDTO.getId());
            }
            return ModelHelper.enrich(studentDTO);
        }/* catch (AccessDeniedException exception) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, exception.getMessage());
        }*/ catch (ResponseStatusException exception) {
            throw exception;
        } catch (Exception exception) {
            exception.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
        }
    }

    @GetMapping("/{studentId}/assignments")
    List<AssignmentDTO> getAssignments(@PathVariable String studentId){
        try {
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
        }catch (TeamServiceException | ExerciseServiceException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage());
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
        }

    }

    @GetMapping("{studentId}/teams/confirm/{token}")
    boolean confirmToken(@PathVariable String token, @PathVariable String studentId) {
        try {
            return notificationService.confirm(token,studentId);
           //todo più eccezioni
        } catch (Exception exception) {
            System.out.println(exception.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
        }
    }

    @GetMapping("{studentId}/teams/reject/{token}")
    boolean rejectToken(@PathVariable String token, @PathVariable String studentId) {
        try {
            return  notificationService.reject(token);
            //todo più eccezioni
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
        }

    }
}
