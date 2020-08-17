package it.polito.ai.backend.controllers;

import io.swagger.v3.oas.annotations.Operation;
import it.polito.ai.backend.dtos.PaperDTO;
import it.polito.ai.backend.dtos.AssignmentDTO;
import it.polito.ai.backend.dtos.StudentDTO;
import it.polito.ai.backend.services.assignment.PaperNotFoundException;
import it.polito.ai.backend.services.assignment.AssignmentNotFoundException;
import it.polito.ai.backend.services.assignment.AssignmentService;
import it.polito.ai.backend.services.team.StudentNotFoundException;
import it.polito.ai.backend.services.team.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;

@RestController
@RequestMapping("/API/papers")
@Validated
public class PaperController {

    @Autowired
    TeamService teamService;
    @Autowired
    AssignmentService assignmentService;

    @Operation(summary = "get paper")
    @GetMapping("/{paperId}")
    ResponseEntity<PaperDTO> getOne(@PathVariable @NotNull Long paperId) {
        PaperDTO paperDTO = assignmentService.getPaper(paperId).orElseThrow(() -> new PaperNotFoundException(paperId.toString()));
        Long exerciseId = assignmentService.getAssignmentForPaper(paperId).map(AssignmentDTO::getId).orElseThrow(() -> new AssignmentNotFoundException(paperId.toString()));
        String studentId = assignmentService.getStudentForPaper(paperId).map(StudentDTO::getId).orElseThrow( () -> new StudentNotFoundException(paperId.toString()));
        return new ResponseEntity<>(ModelHelper.enrich(paperDTO,studentId,exerciseId), HttpStatus.OK);
    }
}
