package it.polito.ai.backend.services.team;

public class CourseNotEnabledException extends TeamServiceConflictException {
    public CourseNotEnabledException(String courseId) {
        super(String.format("course %s not enabled", courseId));
    }
}
