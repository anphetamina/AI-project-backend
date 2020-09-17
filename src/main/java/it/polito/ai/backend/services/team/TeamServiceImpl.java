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
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.IOException;
import java.io.Reader;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
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
    @PreAuthorize("hasRole('TEACHER') and @securityServiceImpl.isAuthorized(#course.getTeacherId())")
    public boolean addCourse(CourseDTO course) {

        if (course.getMin() > course.getMax()) {
            throw new InvalidCourseException("min value cannot exceeds the max one");
        }

        if (!courseRepository.existsById(course.getId())) {
            Course c = modelMapper.map(course, Course.class);
            c.addTeacher(teacherRepository.findById(course.getTeacherId())
                    .orElseThrow(() -> new TeacherNotFoundException(course.getTeacherId())));
            courseRepository.save(c);
            return true;
        }
        return false;
    }

    @Override
    @PreAuthorize("(hasRole('TEACHER') and @securityServiceImpl.isTaught(#id)) or (hasRole('STUDENT') and @securityServiceImpl.isEnrolled(#id))")
    public Optional<CourseDTO> getCourse(String id) {
        return courseRepository.findById(id)
                .map(c -> modelMapper.map(c, CourseDTO.class));
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
    @PreAuthorize("(hasRole('TEACHER')) or (hasRole('STUDENT'))")
    public Optional<StudentDTO> getStudent(String studentId) {
        return studentRepository.findById(studentId)
                .map(s -> modelMapper.map(s, StudentDTO.class));
    }

    @Override
    @PreAuthorize("hasRole('TEACHER')")
    public List<StudentDTO> getAllStudents() {
        return studentRepository.findAll()
                .stream()
                .map(s -> modelMapper.map(s, StudentDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    @PreAuthorize("(hasRole('TEACHER') and @securityServiceImpl.isTaught(#courseId))")
    public List<StudentDTO> getEnrolledStudents(String courseId) {
        Optional<Course> course = courseRepository.findById(courseId);

        if (!course.isPresent()) {
            throw new CourseNotFoundException(courseId);
        }

        return course.get().getStudents()
                .stream()
                .map(s -> modelMapper.map(s, StudentDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    @PreAuthorize("hasRole('TEACHER') and @securityServiceImpl.isTaught(#courseId)")
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
    @PreAuthorize("hasRole('TEACHER') and @securityServiceImpl.isTaught(#courseId)")
    public boolean removeStudentFromCourse(String studentId, String courseId) {
        /**
         * check if the student is enrolled to the course
         */

        Course course = courseRepository.findById(courseId).orElseThrow(() -> new CourseNotFoundException(courseId));

        Student student = course
                .getStudents()
                .stream()
                .filter(s -> s.getId().equals(studentId))
                .findFirst()
                .orElseThrow(() -> new StudentNotFoundException(studentId));

        /**
         * the student can be removed from the course if he/she is not part of any team
         */
        if (student.getTeams().stream().anyMatch(t -> t.getCourse().getId().compareTo(courseId) == 0)) {
            return false;
        }

        course.removeStudent(student);

        return true;
    }

    @Override
    @PreAuthorize("hasRole('TEACHER') and @securityServiceImpl.isTaught(#courseId)")
    public void enableCourse(String courseId) {
        Optional<Course> course = courseRepository.findById(courseId);

        if (!course.isPresent()) {
            throw new CourseNotFoundException(courseId);
        }

        course.get().setEnabled(true);
    }

    @Override
    @PreAuthorize("hasRole('TEACHER') and @securityServiceImpl.isTaught(#courseId)")
    public void disableCourse(String courseId) {
        Optional<Course> course = courseRepository.findById(courseId);

        if (!course.isPresent()) {
            throw new CourseNotFoundException(courseId);
        }

        course.get().setEnabled(false);
    }

    /*@Override
    public List<Boolean> addAll(List<StudentDTO> students) {
        return students.stream()
                .map(s -> addStudent(s))
                .collect(Collectors.toList());
    }*/

    @Override
    @PreAuthorize("hasRole('TEACHER') and @securityServiceImpl.isTaught(#courseId)")
    public List<Boolean> enrollAll(List<String> studentIds, String courseId) {
        return studentIds.stream()
                .map(id -> addStudentToCourse(id, courseId))
                .collect(Collectors.toList());
    }

    @Override
    @PreAuthorize("hasRole('TEACHER') and @securityServiceImpl.isTaught(#courseId)")
    public List<Boolean> addAndEnroll(Reader r, String courseName)  {
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
    @PreAuthorize("(hasRole('STUDENT') and @securityServiceImpl.isAuthorized(#studentId))")
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
    @PreAuthorize("(hasRole('STUDENT') and @securityServiceImpl.isPartOf(#teamId)) or (hasRole('TEACHER') and @securityServiceImpl.isHelping(#teamId))")
    public List<StudentDTO> getMembers(Long teamId) {
        return teamRepository.findById(teamId)
                .orElseThrow(() -> new TeamNotFoundException(teamId.toString()))
                .getMembers()
                .stream()
                .map(s -> modelMapper.map(s, StudentDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    @PreAuthorize("(hasRole('STUDENT') and @securityServiceImpl.isEnrolled(#courseId))")
    public TeamDTO proposeTeam(String courseId, String name, List<String> memberIds) {
        Optional<Course> course = courseRepository.findById(courseId);
        if (!course.isPresent()) {
            throw new CourseNotFoundException("Not exist course with id: "+courseId);
        } else if (!course.get().isEnabled()) {
            throw new CourseNotEnabledException("The course is disable"+ courseId);
        }
        if (memberIds.size() < course.get().getMin()) {
            throw new TeamSizeMinException(courseId, String.valueOf(course.get().getMin()));
        } else if (memberIds.size() > course.get().getMax()) {
            throw new TeamSizeMaxException(courseId, String.valueOf(course.get().getMax()));
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
    @PreAuthorize("(hasRole('STUDENT') and @securityServiceImpl.isAuthorized(#studentId)) and @securityServiceImpl.isEnrolled(#courseId)")
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
    @PreAuthorize("(hasRole('STUDENT') and @securityServiceImpl.isAuthorized(#studentId)) and @securityServiceImpl.isEnrolled(#courseId)")
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
    @PreAuthorize("(hasRole('TEACHER') and @securityServiceImpl.isTaught(#courseId)) or (hasRole('STUDENT') and @securityServiceImpl.isEnrolled(#courseId))")
    public List<TeamDTO> getTeamsForCourse(String courseId) {

        return courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException(courseId))
                .getTeams()
                .stream().filter(team -> team.getStatus().equals(TeamStatus.ACTIVE))
                .map(t -> modelMapper.map(t, TeamDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    @PreAuthorize("(hasRole('TEACHER') and @securityServiceImpl.isTaught(#courseId)) or (hasRole('STUDENT') and @securityServiceImpl.isEnrolled(#courseId))")
    public List<TeacherDTO> getTeachersForCourse(String courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new CourseNotFoundException(courseId))
                .getTeachers()
                .stream()
                .map(t -> modelMapper.map(t, TeacherDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    @PreAuthorize("hasRole('STUDENT') and @securityServiceImpl.isEnrolled(#courseId)")
    public List<StudentDTO> getStudentsInTeams(String courseId) {
        if (!courseRepository.existsById(courseId)) {
            throw new CourseNotFoundException(courseId);
        }

        return courseRepository.getStudentsInTeams(courseId).stream()
                .map(s -> modelMapper.map(s, StudentDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    @PreAuthorize("hasRole('STUDENT') and @securityServiceImpl.isEnrolled(#courseId)")
    public List<StudentDTO> getAvailableStudents(String courseId) {
        if (!courseRepository.existsById(courseId)) {
            throw new CourseNotFoundException(courseId);
        }

        return courseRepository.getStudentsNotInTeams(courseId).stream()
                .map(s -> modelMapper.map(s, StudentDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    @PreAuthorize("hasRole('STUDENT') and @securityServiceImpl.isPartOf(#teamId)")
    public Optional<TeamDTO> getTeam(Long teamId) {
        return teamRepository.findById(teamId).map(t -> modelMapper.map(t, TeamDTO.class));
    }

    @Override
    @PreAuthorize("hasRole('STUDENT') and @securityServiceImpl.isPartOf(#teamId)")
    public Optional<CourseDTO> getCourseForTeam(Long teamId) {
        Course course = teamRepository.findById(teamId)
                .orElseThrow(() -> new TeamNotFoundException(teamId.toString()))
                .getCourse();

        if (course == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(modelMapper.map(course, CourseDTO.class));
    }

    @Override
    @PreAuthorize("hasRole('STUDENT') and @securityServiceImpl.isPartOf(#teamId)")
    public void confirmTeam(Long teamId) {
        Optional<Team> team = teamRepository.findById(teamId);

        if (!team.isPresent()) {
            throw new TeamNotFoundException(teamId.toString());
        }

        team.get().setStatus(TeamStatus.ACTIVE);
    }

    @Override
    @PreAuthorize("hasRole('STUDENT') and @securityServiceImpl.isPartOf(#teamId)")
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
    @PreAuthorize("hasRole('TEACHER') and (@securityServiceImpl.isTaught(#courseId) || @securityServiceImpl.isAuthorized(#teacherId))")
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
    @PreAuthorize("hasRole('TEACHER') and @securityServiceImpl.isAuthorized(#id)")
    public Optional<TeacherDTO> getTeacher(String id) {
        return teacherRepository.findById(id).map(t -> modelMapper.map(t, TeacherDTO.class));
    }

    @Override
    @PreAuthorize("hasRole('TEACHER') and @securityServiceImpl.isAuthorized(#id)")
    public List<CourseDTO> getCoursesForTeacher(String id) {
        return teacherRepository.findById(id)
                .orElseThrow(() -> new TeacherNotFoundException(id))
                .getCourses()
                .stream()
                .map(c -> modelMapper.map(c, CourseDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    @PreAuthorize("(hasRole('TEACHER') and @securityServiceImpl.isTaught(#courseId))")
    public boolean deleteCourse(String courseId) {
        Course course =  courseRepository.findById(courseId).orElseThrow(() -> new CourseNotFoundException(courseId));

        /**
         * a course cannot be deleted if enabled or there are enrolled students
         * having enrolled students means possibly having more entities related to this course
         */
        if (course.isEnabled() || course.getStudents().size() > 0 || course.getTeams().size() > 0 || course.getAssignments().size() > 0) {
            return false;
        }

        course.removeTeachers();
        course.setVirtualMachineModel(null);
        courseRepository.delete(course);

        return true;

    }

    @Override
    @PreAuthorize("(hasRole('TEACHER') and @securityServiceImpl.isTaught(#courseId))")
    public CourseDTO updateCourse(String courseId, CourseDTO courseDTO) {

        if (!courseId.equals(courseDTO.getId())) {
            throw new CourseIdNotCorrespondingException(courseDTO.getId(), courseId);
        }

        Course course = courseRepository.findById(courseDTO.getId()).orElseThrow(() -> new CourseNotFoundException(courseDTO.getId()));

        /**
         * check if the course is enabled
         */
        if (course.isEnabled()) {
            throw new CourseEnabledException(courseDTO.getId());
        }

        String name = courseDTO.getName();


         /** check if the new name is unique*/
         List<Course> courses = courseRepository.findAll();
         /** remove actual course from the list*/
         courses.remove(course);


        if (courses.stream().anyMatch((c -> c.getName().toLowerCase().equals(name.toLowerCase())))) {
            throw new DuplicateCourseNameException(name);
        }

        int min = courseDTO.getMin();
        int max = courseDTO.getMax();

        /**
         * check if there are no teams which size is lower than the new min and greater than the new max
         */
        if (course.getTeams().stream().anyMatch(t -> t.getMembers().size() < min)) {
            throw new TeamSizeMinException(String.valueOf(min), String.valueOf(course.getMin()));
        }
        if (course.getTeams().stream().anyMatch(t -> t.getMembers().size() > max)) {
            throw new TeamSizeMaxException(String.valueOf(max), String.valueOf(course.getMax()));
        }

        course.setName(name);
        course.setMin(min);
        course.setMax(max);

        courseRepository.save(course);
        return courseDTO;
    }




}
