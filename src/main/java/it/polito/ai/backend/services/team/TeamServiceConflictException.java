package it.polito.ai.backend.services.team;

public class TeamServiceConflictException extends TeamServiceException {
    public TeamServiceConflictException(String message) {
        super(message);
    }
}
