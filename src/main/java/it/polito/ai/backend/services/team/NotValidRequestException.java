package it.polito.ai.backend.services.team;

public class NotValidRequestException extends TeamServiceException {
    public NotValidRequestException(String message) {
        super(message);
    }
}
