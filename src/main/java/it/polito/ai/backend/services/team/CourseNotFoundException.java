package it.polito.ai.backend.services.team;

public class CourseNotFoundException extends TeamServiceNotFoundException {
    public CourseNotFoundException(String courseId) {
        super(String.format("course %s not found", courseId));
    }
}
