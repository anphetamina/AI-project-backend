package it.polito.ai.backend.services.notification;

import it.polito.ai.backend.services.team.TeamServiceException;

public class DuplicateTokenException extends TeamServiceException {
    public DuplicateTokenException(String message) {
        super(message);
    }
}
