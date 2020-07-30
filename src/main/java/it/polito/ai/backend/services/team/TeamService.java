package it.polito.ai.backend.services.team;

import it.polito.ai.backend.dtos.CourseDTO;
import it.polito.ai.backend.dtos.StudentDTO;
import it.polito.ai.backend.dtos.TeacherDTO;
import it.polito.ai.backend.dtos.TeamDTO;

import java.io.Reader;
import java.util.List;
import java.util.Optional;

public interface TeamService {
    boolean addCourse(CourseDTO course);

    Optional<CourseDTO> getCourse(String courseId);

    List<CourseDTO> getAllCourses();

    boolean addStudent(StudentDTO student);

    Optional<StudentDTO> getStudent(String studentId);

    List<StudentDTO> getAllStudents();

    List<StudentDTO> getEnrolledStudents(String courseId);

    boolean addStudentToCourse(String studentId, String courseId);

    void enableCourse(String courseId);

    void disableCourse(String courseId);

    List<Boolean> addAll(List<StudentDTO> students);
    List<Boolean> enrollAll(List<String> studentIds, String courseId);
    List<Boolean> addAndEnroll(Reader r, String  courseId);

    List<CourseDTO> getCourses(String studentId);

    List<TeamDTO> getTeamsForStudent(String studentId);
    List<StudentDTO> getMembers(Long teamId);

    TeamDTO proposeTeam(String courseId, String name, List<String> memberIds);
    List<TeamDTO> getProposeTeamsForStudentAndCourse(String studentId, String courseId);


    Optional<TeamDTO> getTeamForStudentAndCourse(String studentId, String courseId);

    List<TeamDTO> getTeamsForCourse(String courseId);
    List<TeacherDTO> getTeachersForCourse(String courseId);

    List<StudentDTO> getStudentsInTeams(String courseId);
    List<StudentDTO> getAvailableStudents(String courseId);

    Optional<TeamDTO> getTeam(Long teamId);
    Optional<CourseDTO> getCourse(Long teamId);
    void confirmTeam(Long teamId);
    void evictTeam(Long teamId);

    boolean addTeacher(TeacherDTO teacher);
    boolean addTeacherToCourse(String teacherId, String courseId);
    Optional<TeacherDTO> getTeacher(String id);
    List<CourseDTO> getCoursesForTeacher(String id);
    void deleteCourse(String courseId);
    boolean update(CourseDTO courseDTO);

}
