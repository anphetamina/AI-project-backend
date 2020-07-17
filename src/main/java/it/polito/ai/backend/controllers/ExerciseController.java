package it.polito.ai.backend.controllers;

import it.polito.ai.backend.dtos.CourseDTO;
import it.polito.ai.backend.dtos.ExerciseDTO;
import it.polito.ai.backend.dtos.TeamDTO;
import it.polito.ai.backend.services.exercise.ExerciseService;
import it.polito.ai.backend.services.team.CourseNotFoundException;
import it.polito.ai.backend.services.team.TeamNotFoundException;
import it.polito.ai.backend.services.team.TeamServiceException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/API/exercises")
public class ExerciseController {
    @Autowired
    ExerciseService exerciseService;
    @Autowired
    ModelMapper modelMapper;

    @GetMapping("/{exerciseId}")
    ExerciseDTO getOne(@PathVariable Long exerciseId) {
        try {
            ExerciseDTO exerciseDTO = exerciseService.getExercise(exerciseId)
                    .orElseThrow(() -> new TeamNotFoundException(exerciseId.toString()));
            String courseName = exerciseService.getCourse(exerciseId)
                    .map(CourseDTO::getName).orElseThrow(() -> new CourseNotFoundException(exerciseId.toString()));
            return ModelHelper.enrich(exerciseDTO, courseName);
        }/* catch (AccessDeniedException exception) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, exception.getMessage());
        }*/ catch (TeamServiceException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage());
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
        }
    }
}
