package it.polito.ai.backend.services.exercise;

public class AssignmentNotFoundException extends ExerciseServiceException {
    public AssignmentNotFoundException(String message) {
        super(message);
    }
}
