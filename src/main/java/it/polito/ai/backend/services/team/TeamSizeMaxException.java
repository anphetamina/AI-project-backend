package it.polito.ai.backend.services.team;

public class TeamSizeMaxException extends TeamServiceConflictException {
    public TeamSizeMaxException(String value, String max) {
        super(String.format("%s value exceeds the allowed maximum value %s", value, max));
    }
}
