package it.polito.ai.backend.services.team;

public class StudentNotEnrolledException extends TeamServiceConflictException {
    public StudentNotEnrolledException(String studentId) {
        super(String.format("student %s not enrolled", studentId));
    }
}
