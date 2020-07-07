package it.polito.ai.backend.services;

public class DuplicateTokenException extends TeamServiceException {
    public DuplicateTokenException(String message) {
        super(message);
    }
}
