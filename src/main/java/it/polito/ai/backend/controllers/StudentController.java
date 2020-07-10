package it.polito.ai.backend.controllers;

import it.polito.ai.backend.dtos.CourseDTO;
import it.polito.ai.backend.dtos.StudentDTO;
import it.polito.ai.backend.dtos.TeamDTO;
import it.polito.ai.backend.services.notification.NotificationService;
import it.polito.ai.backend.services.team.CourseNotFoundException;
import it.polito.ai.backend.services.team.StudentNotFoundException;
import it.polito.ai.backend.services.team.TeamService;
import it.polito.ai.backend.services.team.TeamServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/API/students")
public class StudentController {

    @Autowired
    TeamService teamService;
    @Autowired
    NotificationService notificationService;

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
                        String courseName = teamService.getCourse(t.getId()).map(CourseDTO::getName).orElseThrow(() -> new CourseNotFoundException(t.getId().toString()));
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
}
