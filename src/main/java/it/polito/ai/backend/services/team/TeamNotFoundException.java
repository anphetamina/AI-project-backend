package it.polito.ai.backend.services.team;

public class TeamNotFoundException extends TeamServiceNotFoundException {
    public TeamNotFoundException(String message) {
        super(message);
    }
}
