package it.polito.ai.backend.services.team;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import it.polito.ai.backend.dtos.CourseDTO;
import it.polito.ai.backend.dtos.StudentDTO;
import it.polito.ai.backend.dtos.TeacherDTO;
import it.polito.ai.backend.dtos.TeamDTO;
import it.polito.ai.backend.entities.*;
import it.polito.ai.backend.repositories.CourseRepository;
import it.polito.ai.backend.repositories.StudentRepository;
import it.polito.ai.backend.repositories.TeacherRepository;
import it.polito.ai.backend.repositories.TeamRepository;
import it.polito.ai.backend.services.notification.NotificationService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.Reader;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
// @EnableGlobalMethodSecurity(prePostEnabled = true)
public class TeamServiceImpl implements TeamService {

    @Autowired
    CourseRepository courseRepository;
    @Autowired
    StudentRepository studentRepository;
    @Autowired
    TeamRepository teamRepository;
    @Autowired
    TeacherRepository teacherRepository;
    @Autowired
    ModelMapper modelMapper;
    @Autowired
    NotificationService notificationService;

    @Override
    public boolean addCourse(CourseDTO course) {
        if (!courseRepository.existsById(course.getId())) {
            Course c = modelMapper.map(course, Course.class);
            courseRepository.save(c);
            return true;
        }
        return false;
    }

    @Override
    public Optional<CourseDTO> getCourse(String id) {
        return courseRepository.findById(id)
                .map(c -> modelMapper.map(c, CourseDTO.class));
    }

    @Override
    public List<CourseDTO> getAllCourses() {
        return courseRepository.findAll()
                .stream()
                .map(c -> modelMapper.map(c, CourseDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public boolean addStudent(StudentDTO student) {
        if (!studentRepository.existsById(student.getId())) {
            Student s = modelMapper.map(student, Student.class);

            studentRepository.save(s);
            return true;
        }
        return false;
    }

    @Override
    public Optional<StudentDTO> getStudent(String studentId) {
        return studentRepository.findById(studentId)
                .map(s -> modelMapper.map(s, StudentDTO.class));
    }

    @Override
    public List<StudentDTO> getAllStudents() {
        return studentRepository.findAll()
                .stream()
                .map(s -> modelMapper.map(s, StudentDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<StudentDTO> getEnrolledStudents(String courseName) {
        Optional<Course> course = courseRepository.findById(courseName);

        if (!course.isPresent()) {
            throw new CourseNotFoundException(courseName);
        }

        return course.get().getStudents()
                .stream()
                .map(s -> modelMapper.map(s, StudentDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public boolean addStudentToCourse(String studentId, String courseId) {
        Optional<Student> student = studentRepository.findById(studentId);

        if (!student.isPresent()) {
            throw new StudentNotFoundException(studentId);
        }

        Optional<Course> course = courseRepository.findById(courseId);

        if (!course.isPresent()) {
            throw new CourseNotFoundException(courseId);
        }

        boolean studentAlreadyEnrolled = student.get().getCourses().contains(course.get());
        boolean courseIsEnabled = course.get().isEnabled();

        if (studentAlreadyEnrolled || !courseIsEnabled) {
            return false;
        }

        student.get().addCourse(course.get());
        return true;
    }

    @Override
    public void enableCourse(String courseId) {
        Optional<Course> course = courseRepository.findById(courseId);

        if (!course.isPresent()) {
            throw new CourseNotFoundException(courseId);
        }

        course.get().setEnabled(true);
    }

    @Override
    public void disableCourse(String courseId) {
        Optional<Course> course = courseRepository.findById(courseId);

        if (!course.isPresent()) {
            throw new CourseNotFoundException(courseId);
        }

        course.get().setEnabled(false);
    }

    @Override
    public List<Boolean> addAll(List<StudentDTO> students) {
        return students.stream()
                .map(s -> addStudent(s))
                .collect(Collectors.toList());
    }

    @Override
    public List<Boolean> enrollAll(List<String> studentIds, String courseId) {
        return studentIds.stream()
                .map(id -> addStudentToCourse(id, courseId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Boolean> addAndEnroll(Reader r, String courseName) {
        CsvToBean<StudentDTO> csvToBean = new CsvToBeanBuilder(r)
                .withType(StudentDTO.class)
                .withIgnoreLeadingWhiteSpace(true)
                .build();
        List<StudentDTO> students = csvToBean.parse();

        /*
         * added   enrolled
         * true    true    -> true
         * false   /       -> false
         * true    false   -> false (course not enabled)
         * false   /       -> false
         */

        return students.stream()
                .map(s -> addThenEnroll(s, courseName))
                .collect(Collectors.toList());
    }

    private boolean addThenEnroll(StudentDTO student, String courseId) {
        if (addStudent(student)) {
            return addStudentToCourse(student.getId(), courseId);
        }
        return false;
    }

    @Override
    public List<CourseDTO> getCourses(String studentId) {
        Optional<Student> student = studentRepository.findById(studentId);

        if (!student.isPresent()) {
            throw new StudentNotFoundException(studentId);
        }

        return student.get().getCourses().stream()
                .map(c -> modelMapper.map(c, CourseDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<TeamDTO> getTeamsForStudent(String studentId) {//Solo team attivi
        Optional<Student> student = studentRepository.findById(studentId);

        if (!student.isPresent()) {
            throw new StudentNotFoundException(studentId);
        }

        return student.get().getTeams().stream().filter(team -> team.getStatus().equals(TeamStatus.ACTIVE))
                .map(t -> modelMapper.map(t, TeamDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<StudentDTO> getMembers(Long teamId) {
        Optional<Team> team = teamRepository.findById(teamId);

        if (!team.isPresent()) {
            throw new TeamNotFoundException(teamId.toString());
        }

        return team.get().getMembers().stream()
                .map(s -> modelMapper.map(s, StudentDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public TeamDTO proposeTeam(String courseId, String name, List<String> memberIds) {
        Optional<Course> course = courseRepository.findById(courseId);
        if (!course.isPresent()) {
            throw new CourseNotFoundException(courseId);
        } else if (!course.get().isEnabled()) {
            throw new CourseNotEnabledException(courseId);
        }
        if (memberIds.size() < course.get().getMin()) {
            throw new TeamSizeMinException(name);
        } else if (memberIds.size() > course.get().getMax()) {
            throw new TeamSizeMaxException(name);
        }
        if( course.get().getTeams().stream().anyMatch(team -> team.getName().equals(name)))
            throw new TeamServiceConflictException("Exist name: "+name+" for team in course: "+courseId);


        HashSet<String> uniqueIds = new HashSet<String>();
        Team team = new Team();
        team.setName(name);
        team.setStatus(TeamStatus.UNCONFIRMED);

        List<Student> students = memberIds.stream()
                .map(id -> checkStudent(id, course.get(), uniqueIds))
                .collect(Collectors.toList());

        students.forEach(team::addStudent);
        team.setCourse(course.get());

        teamRepository.save(team);

        return modelMapper.map(team, TeamDTO.class);
    }

    @Override
    public List<TeamDTO> getProposeTeamsForStudentAndCourse(String studentId, String courseId) {
        Optional<Student> student = studentRepository.findById(studentId);
        if(!student.isPresent())
            throw new StudentNotFoundException(studentId);

        Optional<Course> course = courseRepository.findById(courseId);
        if(!course.isPresent())
            throw new CourseNotFoundException(courseId);

        if(!student.get().getCourses().contains(course.get()))
            throw new StudentNotEnrolledException("Student: "+studentId+" is non enrolled to the course: "+courseId);

        return student.get()
                .getTeams()
                .stream()
                .filter(team -> team.getStatus().equals(TeamStatus.UNCONFIRMED) && team.getCourse().getId().equals(courseId))
                .map(t -> modelMapper.map(t, TeamDTO.class))
                .collect(Collectors.toList());
    }




    @Override
    public Optional<TeamDTO> getTeamForStudentAndCourse(String studentId, String courseId) {
        Optional<Student> student = studentRepository.findById(studentId);
        if(!student.isPresent())
            throw new StudentNotFoundException(studentId);

        Optional<Course> course = courseRepository.findById(courseId);
        if(!course.isPresent())
            throw new CourseNotFoundException(courseId);

        if(!student.get().getCourses().contains(course.get()))
            throw new StudentNotEnrolledException("Student: "+studentId+" is non enrolled to the course: "+courseId);

       return student.get().getTeams()
               .stream()
               .filter(team -> team.getCourse().getId().equals(courseId) && team.getStatus().equals(TeamStatus.ACTIVE))
               .findAny()
               .map(t -> modelMapper.map(t,TeamDTO.class));
    }




    private Student checkStudent(String id, Course course, HashSet<String> uniqueIds) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new StudentNotFoundException(id));

        if (!student.getCourses().contains(course)) {
            throw new StudentNotEnrolledException(id);
        }
        if (!uniqueIds.add(student.getId())) {
            throw new DuplicateIdException(id);
        }
        if (student.getTeams().stream().anyMatch(t -> t.getCourse().equals(course) && t.getStatus()==TeamStatus.ACTIVE)) {
            throw new StudentAlreadyInTeamException(id);
        }
        return student;
    }

    @Override
    public List<TeamDTO> getTeamsForCourse(String courseId) {

        return courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException(courseId))
                .getTeams()
                .stream().filter(team -> team.getStatus().equals(TeamStatus.ACTIVE))
                .map(t -> modelMapper.map(t, TeamDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<TeacherDTO> getTeachersForCourse(String courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException(courseId))
                .getTeachers()
                .stream()
                .map(t -> modelMapper.map(t, TeacherDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<StudentDTO> getStudentsInTeams(String courseId) {
        if (!courseRepository.existsById(courseId)) {
            throw new CourseNotFoundException(courseId);
        }

        return courseRepository.getStudentsInTeams(courseId).stream()
                .map(s -> modelMapper.map(s, StudentDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<StudentDTO> getAvailableStudents(String courseId) {
        if (!courseRepository.existsById(courseId)) {
            throw new CourseNotFoundException(courseId);
        }

        return courseRepository.getStudentsNotInTeams(courseId).stream()
                .map(s -> modelMapper.map(s, StudentDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<TeamDTO> getTeam(Long teamId) {
        return teamRepository.findById(teamId)
                .map(t -> modelMapper.map(t, TeamDTO.class));
    }

    @Override
    public Optional<CourseDTO> getCourse(Long teamId) {
        return teamRepository.findById(teamId)
                .map(t -> modelMapper.map(t.getCourse(), CourseDTO.class));
    }

    @Override
    public void confirmTeam(Long teamId) {
        Optional<Team> team = teamRepository.findById(teamId);

        if (!team.isPresent()) {
            throw new TeamNotFoundException(teamId.toString());
        }

        team.get().setStatus(TeamStatus.ACTIVE);
    }

    @Override
    public void evictTeam(Long teamId) {
        Optional<Team> team = teamRepository.findById(teamId);
        if (!team.isPresent()) {
            throw new TeamNotFoundException(teamId.toString());
        }
        if (team.get().getMembers().size() > 0) {
            for (Student s : team.get().getMembers()) {
                s.getTeams().remove(team.get());
            }
            team.get().getMembers().clear();
        }
        teamRepository.delete(team.get());
    }

    @Override
    public boolean addTeacher(TeacherDTO teacher) {
        if (!teacherRepository.existsById(teacher.getId())) {
            Teacher t = modelMapper.map(teacher, Teacher.class);
            teacherRepository.save(t);
            return true;
        }
        return false;
    }

    @Override
    public boolean addTeacherToCourse(String teacherId, String courseId) {
        Optional<Teacher> teacher = teacherRepository.findById(teacherId);

        if (!teacher.isPresent()) {
            throw new TeacherNotFoundException(teacherId);
        }

        Optional<Course> course = courseRepository.findById(courseId);

        if (!course.isPresent()) {
            throw new CourseNotFoundException(courseId);
        }

        if (!course.get().isEnabled() || course.get().getTeachers().contains(teacher.get())) {
            return false;
        }

        teacher.get().addCourse(course.get());
        return true;
    }

    @Override
    public Optional<TeacherDTO> getTeacher(String id) {
        return teacherRepository.findById(id).map(t -> modelMapper.map(t, TeacherDTO.class));
    }

    @Override
    public List<CourseDTO> getCoursesForTeacher(String id) {
        return teacherRepository.findById(id)
                .orElseThrow(() -> new TeacherNotFoundException(id))
                .getCourses()
                .stream()
                .map(c -> modelMapper.map(c, CourseDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteCourse(String courseId) {
        Optional<Course> course =  courseRepository.findById(courseId);
        if(course.isPresent())
            courseRepository.delete(course.get());




    }

    @Override
    public boolean update(CourseDTO courseDTO) {
        if (courseRepository.existsById(courseDTO.getId())) {
            Course c = modelMapper.map(courseDTO, Course.class);
            courseRepository.save(c);
            return true;
        }
        return false;
    }




}
