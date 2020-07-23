package it.polito.ai.backend.services.team;

public class DuplicateIdException extends TeamServiceConflictException {
    public DuplicateIdException(String message) {
        super(message);
    }
}
