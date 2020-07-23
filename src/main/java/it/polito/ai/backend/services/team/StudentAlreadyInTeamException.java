package it.polito.ai.backend.services.team;

public class StudentAlreadyInTeamException extends TeamServiceConflictException {
    public StudentAlreadyInTeamException(String message) {
        super(message);
    }
}
