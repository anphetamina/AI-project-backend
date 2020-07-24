package it.polito.ai.backend.services.team;

public class DuplicateCourseNameException extends TeamServiceConflictException {
    public DuplicateCourseNameException(String courseName) {
        super(String.format("course name %s already exists", courseName));
    }
}
