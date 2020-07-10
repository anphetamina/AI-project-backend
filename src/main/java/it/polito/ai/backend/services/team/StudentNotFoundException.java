package it.polito.ai.backend.services.team;

public class StudentNotFoundException extends TeamServiceException {
    public StudentNotFoundException(String message) {
        super(message);
    }
}
