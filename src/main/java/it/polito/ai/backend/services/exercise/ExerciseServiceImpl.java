package it.polito.ai.backend.services.exercise;

import it.polito.ai.backend.dtos.CourseDTO;
import it.polito.ai.backend.dtos.ExerciseDTO;
import it.polito.ai.backend.entities.*;
import it.polito.ai.backend.repositories.*;
import it.polito.ai.backend.services.team.CourseNotEnabledException;
import it.polito.ai.backend.services.team.CourseNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.IOException;
import java.sql.Timestamp;
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
    ModelMapper modelMapper;

    /**
     * teacher
     */
    @Override
    public ExerciseDTO addExerciseForCourse(String courseId, Timestamp published, Timestamp expired, MultipartFile file) throws IOException {
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

            return modelMapper.map(exercise, ExerciseDTO.class);

    }


    @Override
    public Optional<ExerciseDTO> getExercise(Long id) {
        return exerciseRepository.findById(id)
                .map(e -> modelMapper.map(e, ExerciseDTO.class));
    }

    @Override
    public List<ExerciseDTO> getAllExercises() {
        return exerciseRepository.findAll()
                .stream()
                .map(e -> modelMapper.map(e, ExerciseDTO.class))
                .collect(Collectors.toList());
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


}

