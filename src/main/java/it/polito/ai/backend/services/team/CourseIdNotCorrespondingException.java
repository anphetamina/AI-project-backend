package it.polito.ai.backend.services.team;

public class CourseIdNotCorrespondingException extends TeamServiceConflictException {
    public CourseIdNotCorrespondingException(String actual, String expected) {
        super(String.format("provided %s id does not match with %s", actual, expected));
    }
}
