package it.polito.ai.backend.services.team;

public class DuplicateIdException extends TeamServiceException {
    public DuplicateIdException(String message) {
        super(message);
    }
}
