package it.polito.ai.backend.services.team;

public class TeamSizeMaxException extends TeamServiceConflictException {
    public TeamSizeMaxException(String message) {
        super(message);
    }
}
