package it.polito.ai.backend.repositories;

import it.polito.ai.backend.entities.Assignment;
import it.polito.ai.backend.entities.Exercise;

import org.springframework.data.jpa.repository.JpaRepository;

import java.sql.Timestamp;
import java.util.List;

public interface ExerciseRepository extends JpaRepository<Exercise,Long> {
    List<Exercise> findByExpiredBefore(Timestamp t);

}
