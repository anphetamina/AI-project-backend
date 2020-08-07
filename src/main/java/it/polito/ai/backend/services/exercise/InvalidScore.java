package it.polito.ai.backend.services.exercise;

public class InvalidScore extends ExerciseServiceException{
    public InvalidScore(String message) {
        super(message);
    }
}
