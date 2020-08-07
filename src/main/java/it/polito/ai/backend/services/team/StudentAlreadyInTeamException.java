package it.polito.ai.backend.services.team;

public class StudentAlreadyInTeamException extends TeamServiceConflictException {
    public StudentAlreadyInTeamException(String studentId) {
        super(String.format("student %s already member in team", studentId));
    }
}
