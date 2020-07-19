package it.polito.ai.backend.services.exercise;

import it.polito.ai.backend.dtos.AssignmentDTO;
import it.polito.ai.backend.dtos.CourseDTO;
import it.polito.ai.backend.dtos.ExerciseDTO;
import it.polito.ai.backend.dtos.StudentDTO;
import it.polito.ai.backend.entities.*;
import it.polito.ai.backend.repositories.*;
import it.polito.ai.backend.services.team.CourseNotEnabledException;
import it.polito.ai.backend.services.team.CourseNotFoundException;
import it.polito.ai.backend.services.team.StudentNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import it.polito.ai.backend.services.Utils;

@Service
@Transactional
public class ExerciseServiceImpl implements ExerciseService {
    @Autowired
    CourseRepository courseRepository;
    @Autowired
    ExerciseRepository exerciseRepository;
    @Autowired
    StudentRepository studentRepository;
    @Autowired
    TeacherRepository teacherRepository;
    @Autowired
    AssignmentRepository assignmentRepository;
    @Autowired
    ModelMapper modelMapper;


    @Override
    public void addExerciseForCourse(String courseId, Timestamp published, Timestamp expired, MultipartFile file) throws IOException {
        Optional<Course> course = courseRepository.findById(courseId);

            if (!course.isPresent()) {
                throw new CourseNotFoundException(courseId);
            } else if (!course.get().isEnabled()) {
                throw new CourseNotEnabledException(courseId);
            }

            Exercise exercise = new Exercise();
            exercise.setPublished(published);
            exercise.setExpired(expired);
            exercise.setCourse(course.get());
            exercise.setImage(Utils.getBytes(file));
            exerciseRepository.save(exercise);

    }

    @Override
    public List<AssignmentDTO> getAssignmentByStudentAndExercise(String studentId, Long exerciseId) {
        Optional<Student> student = studentRepository.findById(studentId);
        if(!student.isPresent())
            throw new StudentNotFoundException(studentId);
        Optional<Exercise> exercise = exerciseRepository.findById(exerciseId);
        if(!exercise.isPresent())
            throw new ExerciseNotFoundException(exerciseId.toString());

        return assignmentRepository.findByStudentAndAndExercise(student.get(),exercise.get())
                .stream()
                .sorted(Comparator.comparing(Assignment::getPublished,Timestamp::compareTo))
                .map(a -> modelMapper.map(a,AssignmentDTO.class))
                .collect(Collectors.toList());
    }


    @Override
    public Optional<ExerciseDTO> getExercise(Long id) {
        return exerciseRepository.findById(id)
                .map(e -> modelMapper.map(e, ExerciseDTO.class));
    }

    @Override
    public Optional<AssignmentDTO> getAssignment(Long assignmentId) {
        return assignmentRepository.findById(assignmentId)
                .map(a -> modelMapper.map(a, AssignmentDTO.class));

    }

    @Override
    public Optional<ExerciseDTO> getExerciseForAssignment(Long assignmentId) {
        return assignmentRepository.findById(assignmentId)
                .map(a -> modelMapper.map(a.getExercise(), ExerciseDTO.class));

    }


    @Override
    public List<ExerciseDTO> getExercisesForCourse(String courseId) {
        Optional<Course> course = courseRepository.findById(courseId);
        if(!course.isPresent()){
            throw new CourseNotFoundException(courseId);
        }
        return course.get().getExercises().stream()
                .map(e -> modelMapper.map(e,ExerciseDTO.class ))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<CourseDTO> getCourse(Long exerciseId) {
        return exerciseRepository.findById(exerciseId)
                .map(e -> modelMapper.map(e.getCourse(), CourseDTO.class));
    }

    @Override
    public List<AssignmentDTO> getAssignmentsForExercise(Long exerciseId) {
        Optional<Exercise> exercise = exerciseRepository.findById(exerciseId);
        if(!exercise.isPresent())
            throw  new ExerciseNotFoundException(exerciseId.toString());
        return exercise.get().getAssignments().stream()
                .map(a -> modelMapper.map(a, AssignmentDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<StudentDTO> getStudentForAssignment(Long assignmentId) {
        return assignmentRepository.findById(assignmentId)
               .map(a -> modelMapper.map(a.getStudent(), StudentDTO.class));
    }

    @Override
    public List<AssignmentDTO> getAssignmentsForStudent(String studentId) {

        Optional<Student> student = studentRepository.findById(studentId);
        if(!student.isPresent())
            throw  new StudentNotFoundException(studentId);
        return assignmentRepository.findByStudent(student.get())
                .stream()
                .sorted(Comparator.comparing(Assignment::getPublished,Timestamp::compareTo))
                .map(a -> modelMapper.map(a, AssignmentDTO.class))
                .collect(Collectors.toList());

    }



    @Override
    public AssignmentDTO addAssignmentByte(Timestamp published, AssignmentStatus state, boolean flag, Integer score, Byte[] image, String studentId, Long exerciseId) {
        Optional<Student> student = studentRepository.findById(studentId);
        if(!student.isPresent())
            throw  new StudentNotFoundException(studentId);
        Optional<Exercise> exercise = exerciseRepository.findById(exerciseId);
        if(!exercise.isPresent())
            throw  new ExerciseNotFoundException(exerciseId.toString());

        Assignment assignment = new Assignment();
        assignment.setScore(score);
        assignment.setFlag(flag);
        assignment.setStatus(state);
        assignment.setExercise(exercise.get());
        assignment.setPublished(published);
        assignment.setStudent(student.get());
        assignment.setImage(image);
        assignmentRepository.save(assignment);
        return modelMapper.map(assignment,AssignmentDTO.class);

    }




}

