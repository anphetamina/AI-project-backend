package it.polito.ai.backend.services.assignment;

import it.polito.ai.backend.dtos.*;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

public interface AssignmentService {

    /**
     * teacher
     */
    AssignmentDTO addAssignmentForCourse(String courseId, Timestamp published, Timestamp expired, MultipartFile file) throws IOException;

    /**
     * teacher/student
     */
    List<PaperDTO> getPaperByStudentAndAssignment(String studentId, Long assignmentId);
    Optional<AssignmentDTO> getAssignment(Long id);
    Optional<PaperDTO> getPaper(Long paperId);
    Optional<AssignmentDTO> getAssignmentForPaper(Long paperId);
    List<AssignmentDTO> getAssignmentsForCourse(String courseId);
    Optional<CourseDTO> getCourse(Long assignmentId);
    void setPapersNullForAssignment(Long assignmentId);

    boolean setPapersReadForStudentAndAssignment(Long assignmentId, String studentId);

    Optional<StudentDTO> getStudentForPaper(Long paperId);
    List<PaperDTO> getLastPapers(Long assignmentId);
    PaperDTO addPaperByte(Timestamp published,
                               PaperStatus state,
                               boolean flag, String score,
                               byte[] image,
                               String studentId, Long assignmentId);
    /** student*/
    boolean checkPaper(Long assignmentId, String studentId) ;



}
