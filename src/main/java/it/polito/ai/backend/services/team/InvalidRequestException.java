package it.polito.ai.backend.services.team;

public class InvalidRequestException extends TeamServiceException {
    public InvalidRequestException(String message) {
        super(message);
    }
}
