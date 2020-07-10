package it.polito.ai.backend.services.team;

public class CourseNotFoundException extends TeamServiceException {
    public CourseNotFoundException(String message) {
        super(message);
    }
}
