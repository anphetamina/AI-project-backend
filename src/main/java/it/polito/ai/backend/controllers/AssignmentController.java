package it.polito.ai.backend.controllers;

import it.polito.ai.backend.dtos.AssignmentDTO;
import it.polito.ai.backend.dtos.ExerciseDTO;
import it.polito.ai.backend.dtos.StudentDTO;
import it.polito.ai.backend.services.exercise.AssignmentNotFoundException;
import it.polito.ai.backend.services.exercise.ExerciseServiceException;
import it.polito.ai.backend.services.exercise.ExerciseNotFoundException;
import it.polito.ai.backend.services.exercise.ExerciseService;
import it.polito.ai.backend.services.team.StudentNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/API/assignments")
public class AssignmentController {
    @Autowired
    ExerciseService exerciseService;
    @Autowired
    ModelMapper modelMapper;

    @GetMapping("/{assignmentId}")
   AssignmentDTO getOne(@PathVariable Long assignmentId) {
        try {
            AssignmentDTO assignmentDTO = exerciseService.getAssignment(assignmentId).orElseThrow(() -> new AssignmentNotFoundException(assignmentId.toString()));
            Long exerciseId = exerciseService.getExerciseForAssignment(assignmentId).map(ExerciseDTO::getId).orElseThrow(() -> new ExerciseNotFoundException(assignmentId.toString()));
            String studentId = exerciseService.getStudentForAssignment(assignmentId).map(StudentDTO::getId).orElseThrow( () -> new StudentNotFoundException(assignmentId.toString()));
            return ModelHelper.enrich(assignmentDTO,studentId,exerciseId);
        } catch (ExerciseServiceException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage());
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
        }
    }

}
