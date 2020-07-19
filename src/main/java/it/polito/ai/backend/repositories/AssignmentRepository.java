package it.polito.ai.backend.repositories;

import it.polito.ai.backend.entities.Assignment;
import it.polito.ai.backend.entities.Exercise;
import it.polito.ai.backend.entities.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssignmentRepository extends JpaRepository<Assignment,Long> {
    List<Assignment> findByStudent(Student student);
    List<Assignment> findByStudentAndAndExercise(Student student, Exercise exercise);
}
