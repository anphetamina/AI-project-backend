package it.polito.ai.backend.repositories;

import it.polito.ai.backend.entities.Paper;
import it.polito.ai.backend.entities.Assignment;
import it.polito.ai.backend.entities.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaperRepository extends JpaRepository<Paper,Long> {
    List<Paper> findByStudent(Student student);
    List<Paper> findByStudentAndAssignment(Student student, Assignment assignment);

}
