package it.polito.ai.backend.services;

public class StudentAlreadyInTeamException extends TeamServiceException {
    public StudentAlreadyInTeamException(String message) {
        super(message);
    }
}
