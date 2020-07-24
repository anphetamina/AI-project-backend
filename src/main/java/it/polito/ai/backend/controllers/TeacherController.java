package it.polito.ai.backend.controllers;

import it.polito.ai.backend.dtos.CourseDTO;
import it.polito.ai.backend.dtos.TeacherDTO;
import it.polito.ai.backend.services.team.TeacherNotFoundException;
import it.polito.ai.backend.services.team.TeamService;
import it.polito.ai.backend.services.team.TeamServiceException;
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
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/API/teachers")
@Validated
public class TeacherController {

    @Autowired
    TeamService teamService;

    @GetMapping("/{id}")
    TeacherDTO getOne(@PathVariable @NotBlank String id) {
        return ModelHelper.enrich(teamService.getTeacher(id).orElseThrow(() -> new TeacherNotFoundException(id)));
    }

    @GetMapping("/{id}/courses")
    CollectionModel<CourseDTO> getCourses(@PathVariable @NotBlank String id) {
        List<CourseDTO> courses = teamService.getCoursesForTeacher(id).stream().map(ModelHelper::enrich).collect(Collectors.toList());
        Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(TeacherController.class).getCourses(id)).withSelfRel();
        return CollectionModel.of(courses, selfLink);
    }

    @PostMapping({"", "/"})
    TeacherDTO addTeacher(@RequestBody @Valid TeacherDTO teacherDTO) {
        if (!teamService.addTeacher(teacherDTO)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, String.format("teacher %s already exists", teacherDTO.getId()));
        }
        return ModelHelper.enrich(teacherDTO);
    }
}
