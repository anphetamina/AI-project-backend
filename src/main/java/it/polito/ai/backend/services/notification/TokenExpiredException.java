package it.polito.ai.backend.services.notification;

import it.polito.ai.backend.services.team.TeamServiceException;

public class TokenExpiredException extends TeamServiceException {
    public TokenExpiredException(String message) {
        super(message);
    }
}
