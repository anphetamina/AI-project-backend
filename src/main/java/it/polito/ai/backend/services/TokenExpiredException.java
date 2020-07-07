package it.polito.ai.backend.services;

public class TokenExpiredException extends TeamServiceException {
    public TokenExpiredException(String message) {
        super(message);
    }
}
