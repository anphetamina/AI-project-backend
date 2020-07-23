package it.polito.ai.backend.services.team;

public class StudentNotFoundException extends TeamServiceNotFoundException {
    public StudentNotFoundException(String message) {
        super(message);
    }
}
