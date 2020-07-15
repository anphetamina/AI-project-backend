package it.polito.ai.backend.services.team;

public class TeamServiceNotFoundException extends TeamServiceException {
    public TeamServiceNotFoundException(String message) {
        super(message);
    }
}
