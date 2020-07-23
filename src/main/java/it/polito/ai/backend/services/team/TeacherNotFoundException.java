package it.polito.ai.backend.services.team;

public class TeacherNotFoundException extends TeamServiceNotFoundException {
    public TeacherNotFoundException(String message) {
        super(message);
    }
}
