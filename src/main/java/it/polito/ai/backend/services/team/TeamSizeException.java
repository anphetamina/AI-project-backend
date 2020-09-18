package it.polito.ai.backend.services.team;

public class TeamSizeException extends TeamServiceConflictException {
    public TeamSizeException(String message) {
        super(message);
    }
}