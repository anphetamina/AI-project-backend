package it.polito.ai.backend.repositories;

import it.polito.ai.backend.entities.Assignment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.sql.Timestamp;
import java.util.List;

public interface AssignmentRepository extends JpaRepository<Assignment,Long> {
    List<Assignment> findByExpiredBefore(Timestamp t);

}
