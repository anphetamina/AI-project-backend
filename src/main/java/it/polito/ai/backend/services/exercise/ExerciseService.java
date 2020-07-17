package it.polito.ai.backend.services.exercise;

import it.polito.ai.backend.dtos.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.Reader;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

public interface ExerciseService {

    /**
     * teacher
     */
    ExerciseDTO addExerciseForCourse(String courseId, Timestamp published, Timestamp expired, MultipartFile file) throws IOException;


    /**
     * teacher/student
     */
    Optional<ExerciseDTO> getExercise(Long id);
    List<ExerciseDTO> getAllExercises();
    List<ExerciseDTO> getExercisesForCourse(String courseId);
    public Optional<CourseDTO> getCourse(Long exerciseId);


    /*boolean addAssignment(AssignmentDTO assignmentDTO);

    Optional<AssignmentDTO> getAssignment(String id);

    List<AssignmentDTO> getAllAssignments();
    Metodo per lo stato del Assignment
    List<AssignmentDTO> getAssignmentDTOs(Long exerciseId);*/



}
