package it.polito.ai.backend.services.team;

public class TeamNotFoundException extends TeamServiceException {
    public TeamNotFoundException(String message) {
        super(message);
    }
}
