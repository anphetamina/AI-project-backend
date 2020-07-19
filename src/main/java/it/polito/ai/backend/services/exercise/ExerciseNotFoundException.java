package it.polito.ai.backend.services.exercise;

public class ExerciseNotFoundException extends ExerciseServiceException {
    public ExerciseNotFoundException(String message) {
        super(message);
    }
}
