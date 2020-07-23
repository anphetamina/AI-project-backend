package it.polito.ai.backend.services.team;

public class TeamSizeMinException extends TeamServiceConflictException {
    public TeamSizeMinException(String value, String min) {
        super(String.format("%s value is lower than the allowed min value %s", value, min));
    }
}
