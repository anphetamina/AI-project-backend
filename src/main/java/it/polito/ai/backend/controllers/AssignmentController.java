package it.polito.ai.backend.controllers;

import io.swagger.v3.oas.annotations.Operation;
import it.polito.ai.backend.dtos.*;
import it.polito.ai.backend.repositories.AssignmentRepository;
import it.polito.ai.backend.services.Utils;
import it.polito.ai.backend.services.assignment.*;
import it.polito.ai.backend.dtos.PaperStatus;
import it.polito.ai.backend.services.team.CourseNotFoundException;

import it.polito.ai.backend.services.team.TeamService;
import org.apache.tika.exception.TikaException;
import org.apache.tika.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/API/assignments")
@Validated
public class AssignmentController {

    @Autowired
    TeamService teamService;
    @Autowired
    AssignmentService assignmentService;

    @Operation(summary = "get assignment")
    @GetMapping("/{assignmentId}")
    ResponseEntity<AssignmentDTO> getOne(@PathVariable @NotNull Long assignmentId) {
        AssignmentDTO assignmentDTO = assignmentService.getAssignment(assignmentId)
                .orElseThrow(() -> new AssignmentNotFoundException(assignmentId.toString()));
        String courseId = assignmentService.getCourse(assignmentId)
                .map(CourseDTO::getId).orElseThrow(() -> new CourseNotFoundException(String.format("for assignment %s", assignmentId)));
        return new ResponseEntity<>(ModelHelper.enrich(assignmentDTO, courseId),HttpStatus.OK);
    }

    @Operation(summary = "get the last papers of an assignment")
    @GetMapping("/{assignmentId}/papers")
    ResponseEntity<CollectionModel<PaperDTO>> getLastPapers(@PathVariable @NotNull Long assignmentId ){
        List<PaperDTO> lastPapers = assignmentService.getLastPapers(assignmentId)
                .stream().map(a -> {
                    String studentId = assignmentService.getStudentForPaper(a.getId()).map(StudentDTO::getId)
                            .orElse(null);
                    return ModelHelper.enrich(a,studentId,assignmentId);
                }).collect(Collectors.toList());
        Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(AssignmentController.class).getLastPapers(assignmentId)).withSelfRel();

        return  new ResponseEntity<>(CollectionModel.of(lastPapers,selfLink),HttpStatus.OK);
    }

    @Operation(summary = "get the papers history of an assignment")
    @GetMapping("/{assignmentId}/papers/history")
    ResponseEntity<CollectionModel<PaperDTO>> getHistoryPapers(@PathVariable @NotNull Long assignmentId, @RequestParam @NotBlank String studentId){

        if (studentId.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        List<PaperDTO> paperDTOS =
                assignmentService.getPaperByStudentAndAssignment(studentId,assignmentId)
                .stream().map(a -> ModelHelper.enrich(a,studentId,assignmentId)
                ).collect(Collectors.toList());
        Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(AssignmentController.class).getHistoryPapers(assignmentId,studentId)).withSelfRel();
        for (PaperDTO p:paperDTOS
        ) { System.out.println(p.getId());

        }
        return new ResponseEntity<>(CollectionModel.of(paperDTOS,selfLink),HttpStatus.OK);

    }


    @Operation(summary = "set an paper as read")
    @PostMapping("/{assignmentId}/students/{studentId}/paperRead")
    boolean setReadPaper(@PathVariable @NotNull Long assignmentId, @PathVariable @NotBlank String studentId){
        return assignmentService.setPapersReadForStudentAndAssignment(assignmentId,studentId);
    }

    @Operation(summary = "create a new paper for an assignment")
    @PostMapping("/{assignmentId}/students/{studentId}/papers")
    void submitAssignment(@RequestParam("image") MultipartFile file, @PathVariable @NotBlank String studentId, @PathVariable @NotNull Long assignmentId){

        try {
            Utils.checkTypeImage(file);
            if(assignmentService.checkPaper(assignmentId,studentId))
                assignmentService.addPaperByte(Utils.getNow(), PaperStatus.DELIVERED,false,null,Utils.getBytes(file),studentId,assignmentId);
            else
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, assignmentId.toString());
        } catch (TikaException | IOException e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "invalid file content");
        }


    }

    @Operation(summary = "add a review for a paper by the teacher")
    @PostMapping("/{assignmentId}/paperReview")
    void reviewAssignment(@RequestPart("image") MultipartFile file, @RequestPart @Valid PaperRequest request, @PathVariable @NotNull Long assignmentId){
        try {
            Utils.checkTypeImage(file);
            String studentId = request.getStudentId();
            PaperDTO paperDTO = assignmentService.getPaperByStudentAndAssignment(studentId,assignmentId)
                    .stream()
                    .reduce((a1,a2)-> a2).orElse(null);
            if(paperDTO==null)
                throw  new PaperNotFoundException("There are not any assignment for student: "+studentId);

            boolean flag =  request.isFlag();
            if(!flag && (request.getScore()==null || request.getScore().equals("null")))
                throw  new InvalidScore("The score do not be null");

            if(flag && (request.getScore()!=null && !request.getScore().equals("null")))
                throw  new InvalidScore("The flag must be false if you wont to assign a score");

            if(paperDTO.getStatus()!= PaperStatus.DELIVERED)
                throw new PaperNotFoundException("The student "+studentId +"not update an paper to be reviewed");


            assignmentService.addPaperByte(Utils.getNow(), PaperStatus.REVISED,
                            flag,request.getScore(),Utils.getBytes(file),studentId,assignmentId);


        } catch (TikaException | IOException e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "invalid file content");
        }

    }

}
