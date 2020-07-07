package it.polito.ai.backend.services;

public class DuplicateIdException extends TeamServiceException {
    public DuplicateIdException(String message) {
        super(message);
    }
}
