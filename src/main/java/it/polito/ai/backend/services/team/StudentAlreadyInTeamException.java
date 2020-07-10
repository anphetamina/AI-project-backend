package it.polito.ai.backend.services.team;

public class StudentAlreadyInTeamException extends TeamServiceException {
    public StudentAlreadyInTeamException(String message) {
        super(message);
    }
}
