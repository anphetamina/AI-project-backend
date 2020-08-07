package it.polito.ai.backend.services.exercise;

import it.polito.ai.backend.dtos.*;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
    boolean setAssignmentsNullForExercise(Long exerciseId);
    boolean setAssignmentsReadForStudentAndExercise(Long exerciseId, String studentId);

    Optional<StudentDTO> getStudentForAssignment(Long assignmentId);
    List<AssignmentDTO> getLastAssignments(Long exerciseId);
    AssignmentDTO addAssignmentByte(Timestamp published,
                                    AssignmentStatus state,
                                    boolean flag, String score,
                                    Byte[] image,
                                    String studentId, Long exerciseId);
    /** student*/
    boolean checkAssignment(Long exerciseId, String studentId) ;



}
