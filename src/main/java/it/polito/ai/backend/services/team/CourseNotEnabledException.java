package it.polito.ai.backend.services.team;

public class CourseNotEnabledException extends TeamServiceException {
    public CourseNotEnabledException(String message) {
        super(message);
    }
}
