package it.polito.ai.backend.services.team;

public class CourseEnabledException extends TeamServiceConflictException {
    public CourseEnabledException(String courseId) {
        super(String.format("course %s is enabled", courseId));
    }
}
