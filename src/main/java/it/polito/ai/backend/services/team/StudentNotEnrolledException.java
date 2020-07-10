package it.polito.ai.backend.services.team;

public class StudentNotEnrolledException extends TeamServiceException {
    public StudentNotEnrolledException(String message) {
        super(message);
    }
}
