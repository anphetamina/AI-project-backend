package it.polito.ai.backend.services.team;

public class TeamNotFoundException extends TeamServiceNotFoundException {
    public TeamNotFoundException(String teamId) {
        super(String.format("team %s not found", teamId));
    }
}
