package it.polito.ai.backend.services.team;

public class TeamServiceBadRequestException extends TeamServiceException {
    public TeamServiceBadRequestException(String message) {
        super(message);
    }
}
