package it.polito.ai.backend.services.team;

public class TeamSizeMinException extends TeamServiceConflictException {
    public TeamSizeMinException(String message) {
        super(message);
    }
}
