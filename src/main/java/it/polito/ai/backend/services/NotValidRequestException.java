package it.polito.ai.backend.services;

public class NotValidRequestException extends TeamServiceException {
    public NotValidRequestException(String message) {
        super(message);
    }
}
