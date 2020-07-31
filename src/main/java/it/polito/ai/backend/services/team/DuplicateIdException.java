package it.polito.ai.backend.services.team;

public class DuplicateIdException extends TeamServiceConflictException {
    public DuplicateIdException(String id) {
        super(String.format("duplicate id %s", id));
    }
}
