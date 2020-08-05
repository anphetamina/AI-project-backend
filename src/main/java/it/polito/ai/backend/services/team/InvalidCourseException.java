package it.polito.ai.backend.services.team;

public class InvalidCourseException extends TeamServiceBadRequestException {
    public InvalidCourseException(String message) {
        super(message);
    }
}
