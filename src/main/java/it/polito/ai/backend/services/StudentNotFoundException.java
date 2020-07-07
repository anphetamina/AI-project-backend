package it.polito.ai.backend.services;

public class StudentNotFoundException extends TeamServiceException {
    public StudentNotFoundException(String message) {
        super(message);
    }
}
