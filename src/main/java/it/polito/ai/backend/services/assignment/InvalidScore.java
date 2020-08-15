package it.polito.ai.backend.services.assignment;

public class InvalidScore extends AssignmentServiceException {
    public InvalidScore(String message) {
        super(message);
    }
}
