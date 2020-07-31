package it.polito.ai.backend.services.team;

public class StudentNotFoundException extends TeamServiceNotFoundException {
    public StudentNotFoundException(String studentId) {
        super(String.format("student %s not found", studentId));
    }
}
