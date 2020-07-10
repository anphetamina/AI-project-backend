package it.polito.ai.backend.services.team;

public class TeamSizeMaxException extends TeamServiceException {
    public TeamSizeMaxException(String message) {
        super(message);
    }
}
