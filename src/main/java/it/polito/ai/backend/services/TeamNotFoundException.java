package it.polito.ai.backend.services;

public class TeamNotFoundException extends TeamServiceException {
    public TeamNotFoundException(String message) {
        super(message);
    }
}
