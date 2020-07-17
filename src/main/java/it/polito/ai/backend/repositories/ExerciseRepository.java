package it.polito.ai.backend.repositories;

import it.polito.ai.backend.entities.Exercise;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExerciseRepository extends JpaRepository<Exercise,Long> {
    @EntityGraph(attributePaths={"profilePicture"})
    Exercise findWithPropertyPictureAttachedById(String id);
}
