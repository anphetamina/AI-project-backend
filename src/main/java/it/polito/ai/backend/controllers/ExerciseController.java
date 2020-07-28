package it.polito.ai.backend.controllers;

import io.swagger.v3.oas.annotations.Operation;
import it.polito.ai.backend.dtos.AssignmentDTO;
import it.polito.ai.backend.dtos.CourseDTO;
import it.polito.ai.backend.dtos.ExerciseDTO;
import it.polito.ai.backend.dtos.StudentDTO;
import it.polito.ai.backend.services.Utils;
import it.polito.ai.backend.services.exercise.AssignmentNotFoundException;
import it.polito.ai.backend.services.exercise.AssignmentStatus;
import it.polito.ai.backend.services.exercise.ExerciseNotFoundException;
import it.polito.ai.backend.services.exercise.ExerciseService;
import it.polito.ai.backend.services.team.CourseNotFoundException;
import it.polito.ai.backend.services.team.StudentNotFoundException;
import it.polito.ai.backend.services.team.TeamService;
import org.apache.tika.exception.TikaException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
    List<AssignmentDTO> getLastAssignments(@PathVariable @NotNull Long exerciseId ){

        // todo collection model
        Optional<CourseDTO> courseDTO = exerciseService.getCourse(exerciseId);
        if(!courseDTO.isPresent())
            throw new CourseNotFoundException(exerciseId.toString());
        List<StudentDTO> students = teamService.getEnrolledStudents(courseDTO.get().getId());
        List<AssignmentDTO> lastAssignments = new ArrayList<AssignmentDTO>();
        for (StudentDTO student:students) {
            AssignmentDTO lastAssignment = exerciseService.getAssignmentsForStudent(student.getId())
                    .stream().reduce((a1,a2)-> a2).orElse(null);
            if(lastAssignment==null)
                throw  new AssignmentNotFoundException(student.getId());
            lastAssignments.add(lastAssignment);

        }
        List<AssignmentDTO> assignmentDTOS= new ArrayList<>();
        for (AssignmentDTO a:lastAssignments) {
            String studentId = exerciseService.getStudentForAssignment(a.getId()).map(StudentDTO::getId).orElseThrow( () -> new StudentNotFoundException(a.getId().toString()));
            assignmentDTOS.add(ModelHelper.enrich(a,studentId,exerciseId));

        }

        return  assignmentDTOS;
    }

    @Operation(summary = "get the assignments history of an exercise")
    @GetMapping("/{exerciseId}/history")
    List<AssignmentDTO> getHistoryAssignments(@PathVariable @NotNull Long exerciseId,@RequestBody Map<String,String> map ){

        // todo collection model
        List<AssignmentDTO> assignmentDTOS =
                exerciseService.getAssignmentByStudentAndExercise(map.get("studentId"),exerciseId);
        System.out.println(assignmentDTOS.size());
        List<AssignmentDTO> assignmentDTOList = new ArrayList<>();
        for (AssignmentDTO a:assignmentDTOS) {
            String studentId = exerciseService.getStudentForAssignment(a.getId()).map(StudentDTO::getId).orElseThrow( () -> new StudentNotFoundException(a.getId().toString()));
            assignmentDTOList.add(ModelHelper.enrich(a,studentId,exerciseId));

        }
        return  assignmentDTOList;

    }

    @Operation(summary = "set an  assignment as null for all students enrolled at the courses ")
    @PostMapping("/{exerciseId}/assignmentNull")
    void setNullAssignment(@PathVariable @NotNull Long exerciseId){
        /*No duplicati*/
        List<AssignmentDTO> assignments = exerciseService.getAssignmentsForExercise(exerciseId);
        if(!assignments.isEmpty())
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        /*Per ogni studente iscritto al corso aggiungere un'elaborato con stato null*/
        Optional<CourseDTO> courseDTO = exerciseService.getCourse(exerciseId);
        if(!courseDTO.isPresent())
            throw new CourseNotFoundException(exerciseId.toString());
        Optional<ExerciseDTO> exercise = exerciseService.getExercise(exerciseId);
        if(!exercise.isPresent())
            throw  new ExerciseNotFoundException(exerciseId.toString());
        List<StudentDTO> students = teamService.getEnrolledStudents(courseDTO.get().getId());
        for (StudentDTO student:students) {
            exerciseService.addAssignmentByte(
                    Utils.getNow(),
                    AssignmentStatus.NULL,
                    true,null,exercise.get().getImage(),student.getId(),exerciseId);
        }
    }

    @Operation(summary = "set an assignment as read")
    @PostMapping("/{exerciseId}/assignmentRead")
    void setReadAssignment(@PathVariable @NotNull Long exerciseId, @RequestBody Map<String,String> map){
        if (!map.containsKey("studentId")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        String studentId = map.get("studentId");

        if (studentId == null || studentId.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        List<AssignmentDTO> assignments = exerciseService.getAssignmentByStudentAndExercise(map.get("studentId"),exerciseId);
        AssignmentDTO assignment = assignments.stream().reduce((a1,a2)-> a2).orElse(null);

        if(assignment==null)
            throw  new AssignmentNotFoundException(map.get("studentId"));

        Byte[] image = assignment.getImage();
        if(assignment.getStatus()==AssignmentStatus.NULL ||
                (assignment.getStatus()==AssignmentStatus.RIVSTO && assignment.isFlag()))
            exerciseService.addAssignmentByte(Utils.getNow(),
                    AssignmentStatus.LETTO,true,null,image,map.get("studentId"),exerciseId);
        else
            throw new ResponseStatusException(HttpStatus.CONFLICT, exerciseId.toString());
    }

    @Operation(summary = "create a new assignment for an exercise")
    @PostMapping("/{exerciseId}/assignments")
    void submitAssignment(@RequestParam("image") MultipartFile file, @RequestParam Map<String, String> map, @PathVariable @NotNull Long exerciseId){
        /*Lo studente può caricare solo una soluzione prima che il docente gli dia il permesso per rifralo*/

        if (!map.containsKey("studentId")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        String studentId = map.get("studentId");

        if (studentId.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        try {
            Utils.checkTypeImage(file);
            Optional<ExerciseDTO> exercise = exerciseService.getExercise(exerciseId);
            if(!exercise.isPresent())
                throw  new ExerciseNotFoundException(exerciseId.toString());

            List<AssignmentDTO> assignments = exerciseService.getAssignmentByStudentAndExercise(map.get("studentId"),exerciseId);
            AssignmentDTO assignment = assignments.stream().reduce((a1,a2)-> a2).orElse(null);
            if(assignment==null)
                throw  new AssignmentNotFoundException(map.get("studentId"));

            if(exercise.get().getExpired().after(Utils.getNow()) && assignment.isFlag() && assignment.getStatus()==AssignmentStatus.LETTO)
                exerciseService.addAssignmentByte(Utils.getNow(),AssignmentStatus.CONSEGNATO,false,null,Utils.getBytes(file),map.get("studentId"),exerciseId);
            else
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, exerciseId.toString());
        } catch (TikaException | IOException e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "invalid file content");
        }


    }

    @Operation(summary = "add a review for an assignment by the teacher")
    @PostMapping("/{exerciseId}/assignmentReview")
    void reviewAssignment(@RequestParam("image") MultipartFile file, @RequestParam Map<String, String> map, @PathVariable @NotNull Long exerciseId){
        /*Se il falg=false allora c'è anche il voto
         * se è true allora non c'è il voto*/
        if(!map.containsKey("flag") && !map.containsKey("studentId")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        String studentId = map.get("studentId");

        if (studentId.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        try {
            Utils.checkTypeImage(file);
            List<AssignmentDTO> assignments = exerciseService.getAssignmentByStudentAndExercise(map.get("studentId"),exerciseId);
            AssignmentDTO assignment = assignments.stream().reduce((a1,a2)-> a2).orElse(null);
            if(assignment==null)
                throw  new AssignmentNotFoundException(map.get("studentId"));

            boolean flag =  Boolean.parseBoolean(map.get("flag"));
            if(assignment.getStatus()==AssignmentStatus.CONSEGNATO){
                if(flag)
                    exerciseService.addAssignmentByte(Utils.getNow(),AssignmentStatus.RIVSTO,
                            flag,null,Utils.getBytes(file),map.get("studentId"),exerciseId);
                else {
                    if(map.containsKey("score")){

                        Integer score = Integer.parseInt(map.get("score"));
                        System.out.println(score);
                        exerciseService.addAssignmentByte(Utils.getNow(),AssignmentStatus.RIVSTO,
                                flag,score,Utils.getBytes(file),map.get("studentId"),exerciseId);
                    }else {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
                    }
                }
            }
        } catch (TikaException | IOException e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "invalid file content");
        }

    }
}
