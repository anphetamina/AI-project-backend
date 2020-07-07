package it.polito.ai.backend.controllers;

import it.polito.ai.backend.dtos.CourseDTO;
import it.polito.ai.backend.dtos.TeacherDTO;
import it.polito.ai.backend.services.TeacherNotFoundException;
import it.polito.ai.backend.services.TeamService;
import it.polito.ai.backend.services.TeamServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/API/teachers")
public class TeacherController {

    @Autowired
    TeamService teamService;

    @GetMapping("/{id}")
    TeacherDTO getOne(@PathVariable String id) {
        try {
            return ModelHelper.enrich(teamService.getTeacher(id).orElseThrow(() -> new TeacherNotFoundException(id)));
        } catch (AccessDeniedException exception) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, exception.getMessage());
        } catch(TeamServiceException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage());
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
        }
    }

    @GetMapping("/{id}/courses")
    CollectionModel<CourseDTO> getCourses(@PathVariable String id) {
        try {
            List<CourseDTO> courses = teamService.getCoursesForTeacher(id).stream().map(ModelHelper::enrich).collect(Collectors.toList());
            Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(TeacherController.class).getCourses(id)).withSelfRel();
            return new CollectionModel<>(courses, selfLink);
        } catch (AccessDeniedException exception) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, exception.getMessage());
        } catch (TeamServiceException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage());
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
        }
    }

    @PostMapping({"", "/"})
    TeacherDTO addTeacher(@RequestBody @Valid TeacherDTO teacherDTO) {
        try {
            if (!teamService.addTeacher(teacherDTO)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, teacherDTO.getId());
            }
            return ModelHelper.enrich(teacherDTO);
        } catch (AccessDeniedException exception) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, exception.getMessage());
        } catch (ResponseStatusException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
        }
    }
}
