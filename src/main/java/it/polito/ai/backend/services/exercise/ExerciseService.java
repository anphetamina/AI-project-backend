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
    void addExerciseForCourse(String courseId, Timestamp published, Timestamp expired, MultipartFile file) throws IOException;

    /**
     * teacher/student
     */
    List<AssignmentDTO> getAssignmentByStudentAndExercise(String studentId, Long exerciseId);
    Optional<ExerciseDTO> getExercise(Long id);
    Optional<AssignmentDTO> getAssignment(Long assignmentId);
    Optional<ExerciseDTO> getExerciseForAssignment(Long assignmentId);
    List<ExerciseDTO> getExercisesForCourse(String courseId);
    Optional<CourseDTO> getCourse(Long exerciseId);
    List<AssignmentDTO> getAssignmentsForExercise(Long exerciseId);
    Optional<StudentDTO> getStudentForAssignment(Long assignmentId);
    List<AssignmentDTO> getAssignmentsForStudent(String studentId);
    AssignmentDTO addAssignmentByte(Timestamp published,
                       AssignmentStatus state,
                       boolean flag, Integer score,
                       Byte[] image,
                       String studentId, Long exerciseId);



}
