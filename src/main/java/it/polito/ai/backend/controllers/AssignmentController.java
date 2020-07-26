package it.polito.ai.backend.controllers;

import it.polito.ai.backend.dtos.AssignmentDTO;
import it.polito.ai.backend.dtos.ExerciseDTO;
import it.polito.ai.backend.dtos.StudentDTO;
import it.polito.ai.backend.services.exercise.AssignmentNotFoundException;
import it.polito.ai.backend.services.exercise.ExerciseNotFoundException;
import it.polito.ai.backend.services.exercise.ExerciseService;
import it.polito.ai.backend.services.team.StudentNotFoundException;
import it.polito.ai.backend.services.team.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;

@RestController
@RequestMapping("/API/assignments")
@Validated
public class AssignmentController {

    @Autowired
    TeamService teamService;
    @Autowired
    ExerciseService exerciseService;


    @GetMapping("/{assignmentId}")
    AssignmentDTO getOne(@PathVariable @NotNull Long assignmentId) {
        AssignmentDTO assignmentDTO = exerciseService.getAssignment(assignmentId).orElseThrow(() -> new AssignmentNotFoundException(assignmentId.toString()));
        Long exerciseId = exerciseService.getExerciseForAssignment(assignmentId).map(ExerciseDTO::getId).orElseThrow(() -> new ExerciseNotFoundException(assignmentId.toString()));
        String studentId = exerciseService.getStudentForAssignment(assignmentId).map(StudentDTO::getId).orElseThrow( () -> new StudentNotFoundException(assignmentId.toString()));
        return ModelHelper.enrich(assignmentDTO,studentId,exerciseId);
    }
}
