package it.polito.ai.backend.services;

public class StudentNotEnrolledException extends TeamServiceException {
    public StudentNotEnrolledException(String message) {
        super(message);
    }
}
