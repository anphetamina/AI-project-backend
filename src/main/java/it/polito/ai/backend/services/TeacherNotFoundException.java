package it.polito.ai.backend.services;

public class TeacherNotFoundException extends TeamServiceException {
    public TeacherNotFoundException(String message) {
        super(message);
    }
}
