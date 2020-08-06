package it.polito.ai.backend.controllers;

import io.swagger.v3.oas.annotations.Operation;
import it.polito.ai.backend.dtos.*;
import it.polito.ai.backend.services.Utils;
import it.polito.ai.backend.services.exercise.AssignmentNotFoundException;
import it.polito.ai.backend.services.exercise.AssignmentStatus;
import it.polito.ai.backend.services.exercise.ExerciseNotFoundException;
import it.polito.ai.backend.services.exercise.ExerciseService;
import it.polito.ai.backend.services.team.CourseNotFoundException;

import it.polito.ai.backend.services.team.TeamService;
import org.apache.tika.exception.TikaException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/API/exercises")
@Validated
public class ExerciseController {

    @Autowired
    TeamService teamService;
    @Autowired
    ExerciseService exerciseService;

    @Operation(summary = "get exercise")
    @GetMapping("/{exerciseId}")
    ExerciseDTO getOne(@PathVariable @NotNull Long exerciseId) {
        ExerciseDTO exerciseDTO = exerciseService.getExercise(exerciseId)
                .orElseThrow(() -> new ExerciseNotFoundException(exerciseId.toString()));
        String courseId = exerciseService.getCourse(exerciseId)
                .map(CourseDTO::getId).orElseThrow(() -> new CourseNotFoundException(String.format("for exercise %s", exerciseId)));
        return ModelHelper.enrich(exerciseDTO, courseId);
    }

    @Operation(summary = "get the last assignments of an exercise")
    @GetMapping("/{exerciseId}/assignments")
    CollectionModel<AssignmentDTO> getLastAssignments(@PathVariable @NotNull Long exerciseId ){
        List<AssignmentDTO> lastAssignments = exerciseService.getLastAssignments(exerciseId)
                .stream().map(a -> {
                    String studentId = exerciseService.getStudentForAssignment(a.getId()).map(StudentDTO::getId)
                            .orElse(null);
                    return ModelHelper.enrich(a,studentId,exerciseId);
                }).collect(Collectors.toList());
        Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(ExerciseController.class).getLastAssignments(exerciseId)).withSelfRel();

        return  CollectionModel.of(lastAssignments,selfLink);
    }

    @Operation(summary = "get the assignments history of an exercise")
    @GetMapping("/{exerciseId}/history")
    CollectionModel<AssignmentDTO> getHistoryAssignments(@PathVariable @NotNull Long exerciseId, @RequestParam @NotBlank String studentId){

        if (studentId.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        List<AssignmentDTO> assignmentDTOS =
                exerciseService.getAssignmentByStudentAndExercise(studentId,exerciseId)
                .stream().map(a -> ModelHelper.enrich(a,studentId,exerciseId)
                ).collect(Collectors.toList());
        Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(ExerciseController.class).getHistoryAssignments(exerciseId,studentId)).withSelfRel();

        return  CollectionModel.of(assignmentDTOS,selfLink);

    }

    @Operation(summary = "set an  assignment as null for all students enrolled at the courses ")
    @PostMapping("/{exerciseId}/assignmentNull")
    boolean setNullAssignment(@PathVariable @NotNull Long exerciseId){
       return exerciseService.setAssignmentsNullForExercise(exerciseId);
    }

    @Operation(summary = "set an assignment as read")
    @PostMapping("/{exerciseId}/students/{studentId}/assignmentRead")
    boolean setReadAssignment(@PathVariable @NotNull Long exerciseId, @PathVariable @NotBlank String studentId){
        return exerciseService.setAssignmentsReadForStudentAndExercise(exerciseId,studentId);
    }

    @Operation(summary = "create a new assignment for an exercise")
    @PostMapping("/{exerciseId}/students/{studentId}/assignments")
    void submitAssignment(@RequestParam("image") MultipartFile file, @PathVariable @NotBlank String studentId, @PathVariable @NotNull Long exerciseId){

        try {
            Utils.checkTypeImage(file);
            if(exerciseService.checkAssignment(exerciseId,studentId))
                exerciseService.addAssignmentByte(Utils.getNow(),AssignmentStatus.CONSEGNATO,false,null,Utils.getBytes(file),studentId,exerciseId);
            else
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, exerciseId.toString());
        } catch (TikaException | IOException e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "invalid file content");
        }


    }

    @Operation(summary = "add a review for an assignment by the teacher")
    @PostMapping("/{exerciseId}/assignmentReview")
    void reviewAssignment(@RequestParam("image") MultipartFile file, @RequestParam @Valid AssignmentRequest request, @PathVariable @NotNull Long exerciseId){
        /*Se il falg=false allora c'è anche il voto
         * se è true allora non c'è il voto*/
        String studentId = request.getStudentId();
        try {
            Utils.checkTypeImage(file);
            AssignmentDTO assignment = exerciseService.getAssignmentByStudentAndExercise(studentId,exerciseId)
                    .stream()
                    .reduce((a1,a2)-> a2).orElse(null);
            if(assignment==null)
                throw  new AssignmentNotFoundException(studentId);

            boolean flag =  request.isFlag();
            if(assignment.getStatus()==AssignmentStatus.CONSEGNATO){
                exerciseService.addAssignmentByte(Utils.getNow(),AssignmentStatus.RIVSTO,
                            flag,request.getScore(),Utils.getBytes(file),studentId,exerciseId);

            }
        } catch (TikaException | IOException e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "invalid file content");
        }

    }
}
