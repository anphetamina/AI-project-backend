package it.polito.ai.backend.services.team;

public class TeacherNotFoundException extends TeamServiceException {
    public TeacherNotFoundException(String message) {
        super(message);
    }
}
