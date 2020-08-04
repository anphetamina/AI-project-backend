package it.polito.ai.backend.security;

public class DuplicateConfirmationToken extends SecurityServiceException {
    public DuplicateConfirmationToken(String message) {
        super(message);
    }
}
