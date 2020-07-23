package it.polito.ai.backend.services.team;

public class StudentNotEnrolledException extends TeamServiceConflictException {
    public StudentNotEnrolledException(String message) {
        super(message);
    }
}
