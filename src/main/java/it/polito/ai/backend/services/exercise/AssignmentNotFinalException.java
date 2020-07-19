package it.polito.ai.backend.services.exercise;

public class AssignmentNotFinalException extends ExerciseServiceException {
    public AssignmentNotFinalException(String message) {
        super(message);
    }
}
