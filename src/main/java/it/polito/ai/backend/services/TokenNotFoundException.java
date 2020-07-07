package it.polito.ai.backend.services;

public class TokenNotFoundException extends TeamServiceException {
    public TokenNotFoundException(String message) {
        super(message);
    }
}
