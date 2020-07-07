package it.polito.ai.backend.services;

public class CourseNotEnabledException extends TeamServiceException {
    public CourseNotEnabledException(String message) {
        super(message);
    }
}
