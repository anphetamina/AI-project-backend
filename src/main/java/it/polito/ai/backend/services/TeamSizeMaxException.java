package it.polito.ai.backend.services;

public class TeamSizeMaxException extends TeamServiceException {
    public TeamSizeMaxException(String message) {
        super(message);
    }
}
