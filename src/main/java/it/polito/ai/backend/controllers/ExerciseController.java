package it.polito.ai.backend.controllers;

import it.polito.ai.backend.dtos.AssignmentDTO;
import it.polito.ai.backend.dtos.CourseDTO;
import it.polito.ai.backend.dtos.ExerciseDTO;
import it.polito.ai.backend.dtos.StudentDTO;
import it.polito.ai.backend.services.Utils;
import it.polito.ai.backend.services.exercise.*;
import it.polito.ai.backend.services.team.*;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/API/exercises")
public class ExerciseController {
    @Autowired
    ExerciseService exerciseService;
    @Autowired
    TeamService teamService;
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
        }catch (ExerciseServiceException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage());
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
        }
    }

    @GetMapping("/{exerciseId}/assignments")
    CollectionModel<AssignmentDTO> getLastAssignments(@PathVariable Long exerciseId ){
        try {
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
            lastAssignments.stream().map(a -> {
                String studentId = exerciseService.getStudentForAssignment(a.getId()).map(StudentDTO::getId).orElseThrow( () -> new StudentNotFoundException(a.getId().toString()));
                return  ModelHelper.enrich(a,studentId,exerciseId);
            }).collect(Collectors.toList());
            System.out.println(lastAssignments.size());
            Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(ExerciseController.class).getLastAssignments(exerciseId)).withSelfRel();
            return  CollectionModel.of(lastAssignments, selfLink);


        }catch (TeamServiceException | ExerciseServiceException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage());
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
        }
    }

    @GetMapping("/{exerciseId}/history")
    List<AssignmentDTO> getHistoryAssignments(@PathVariable Long exerciseId,@RequestBody Map<String,String> map ){
        if(map.containsKey("studentId")){
            try{

                List<AssignmentDTO> assignmentDTOS =
                        exerciseService.getAssignmentByStudentAndExercise(map.get("studentId"),exerciseId)
                                .stream()
                                .map(a -> {
                                    String studentId = exerciseService.getStudentForAssignment(a.getId()).map(StudentDTO::getId).orElseThrow( () -> new StudentNotFoundException(a.getId().toString()));
                                    return  ModelHelper.enrich(a,studentId,exerciseId);
                                }).collect(Collectors.toList());;
                System.out.println(assignmentDTOS.size());

                return  assignmentDTOS;

            }catch (TeamServiceException | ExerciseServiceException exception) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage());
            } catch (Exception exception) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
            }

        }else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

    }




    @PostMapping("/{exerciseId}/assignmentNull")
    void setNullAssignment(@PathVariable Long exerciseId){
        try{
            /*No duplicati*/
            List<AssignmentDTO> assignments = exerciseService.getAssignmentsForExercise(exerciseId);
            if(!assignments.isEmpty())
                throw new Exception(exerciseId.toString());
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

        }catch (TeamNotFoundException | ExerciseServiceException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage());
        } catch (Exception exception ) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
        }
    }

    @PostMapping("/{exerciseId}/assignmentRead")
    void setReadAssignment(@PathVariable Long exerciseId, @RequestBody Map<String,String> map){
        if (map.containsKey("studentId")) {
            try {
                List<AssignmentDTO> assignments = exerciseService.getAssignmentsForStudent(map.get("studentId"));
                AssignmentDTO assignment = assignments.stream().reduce((a1,a2)-> a2).orElse(null);

                if(assignment==null)
                    throw  new AssignmentNotFoundException(map.get("studentId"));

                Byte[] image = assignment.getImage();
                if(assignment.getStatus()==AssignmentStatus.NULL ||
                        (assignment.getStatus()==AssignmentStatus.RIVSTO && assignment.isFlag()))
                     exerciseService.addAssignmentByte(Utils.getNow(),
                            AssignmentStatus.LETTO,true,null,image,map.get("studentId"),exerciseId);
                else
                    throw new Exception("Duplicate assignment "+exerciseId.toString());


            }catch (ExerciseServiceException exception) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage());
            } catch (Exception exception) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
            }
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }

    @PostMapping("/{exerciseId}/assignmentSubmit")
    void submitAssignment(@RequestParam("image") MultipartFile file, @RequestParam Map<String, String> map, @PathVariable Long exerciseId){
        if (map.containsKey("studentId")){
            try {

                Utils.checkTypeImage(file);
                Optional<ExerciseDTO> exercise = exerciseService.getExercise(exerciseId);
                if(!exercise.isPresent())
                    throw  new ExerciseNotFoundException(exerciseId.toString());

                List<AssignmentDTO> assignments = exerciseService.getAssignmentsForStudent(map.get("studentId"));
                AssignmentDTO assignment = assignments.stream().reduce((a1,a2)-> a2).orElse(null);
                if(assignment==null)
                    throw  new AssignmentNotFoundException(map.get("studentId"));

                if(exercise.get().getExpired().after(Utils.getNow()) && assignment.isFlag() && assignment.getStatus()==AssignmentStatus.LETTO)
                    exerciseService.addAssignmentByte(Utils.getNow(),AssignmentStatus.CONSEGNATO,true,null,Utils.getBytes(file),map.get("studentId"),exerciseId);
                else
                    throw new Exception(exerciseId.toString());

            }catch (ExerciseServiceException exception) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage());
            } catch (ResponseStatusException exception) {
                throw exception;
            } catch (Exception exception) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
            }
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

    }

    @PostMapping("/{exerciseId}/assignmentReview")
    void reviewAssignment(@RequestParam("image") MultipartFile file, @RequestParam Map<String, String> map, @PathVariable Long exerciseId){
       /*Se il falg=false allora c'è anche il voto
       * se è true allora non c'è il voto*/
        if(map.containsKey("flag") && map.containsKey("studentId")){
            try {

                Utils.checkTypeImage(file);
                List<AssignmentDTO> assignments = exerciseService.getAssignmentsForStudent(map.get("studentId"));
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
                            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
                        }
                    }
                }

            }catch (ExerciseServiceException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage());
            } catch (ResponseStatusException exception) {
                throw exception;
            } catch (Exception exception) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
            }

        }else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }






}
