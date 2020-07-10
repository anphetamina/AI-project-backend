package it.polito.ai.backend.services.notification;

import it.polito.ai.backend.services.team.TeamServiceException;

public class TokenNotFoundException extends TeamServiceException {
    public TokenNotFoundException(String message) {
        super(message);
    }
}
