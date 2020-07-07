package it.polito.ai.backend.controllers;

import it.polito.ai.backend.dtos.CourseDTO;
import it.polito.ai.backend.dtos.StudentDTO;
import it.polito.ai.backend.dtos.TeamDTO;
import it.polito.ai.backend.services.CourseNotFoundException;
import it.polito.ai.backend.services.TeamNotFoundException;
import it.polito.ai.backend.services.TeamService;
import it.polito.ai.backend.services.TeamServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/API/teams")
public class TeamController {

    @Autowired
    TeamService teamService;

    @GetMapping("/{teamId}")
    TeamDTO getOne(@PathVariable Long teamId) {
        try {
            TeamDTO teamDTO = teamService.getTeam(teamId).orElseThrow(() -> new TeamNotFoundException(teamId.toString()));
            String courseName = teamService.getCourse(teamId).map(CourseDTO::getName).orElseThrow(() -> new CourseNotFoundException(teamId.toString()));
            return ModelHelper.enrich(teamDTO, courseName);
        } catch (AccessDeniedException exception) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, exception.getMessage());
        } catch (TeamServiceException exception) {
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
            return new CollectionModel<>(students, selfLink);
        } catch (AccessDeniedException exception) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, exception.getMessage());
        } catch (TeamServiceException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage());
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
        }
    }
}
