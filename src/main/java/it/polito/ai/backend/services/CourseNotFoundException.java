package it.polito.ai.backend.services;

public class CourseNotFoundException extends TeamServiceException {
    public CourseNotFoundException(String message) {
        super(message);
    }
}
