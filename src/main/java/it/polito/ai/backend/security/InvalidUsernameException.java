package it.polito.ai.backend.security;

import javax.security.sasl.AuthenticationException;

public class InvalidUsernameException extends SecurityServiceException {
    public InvalidUsernameException() {
        super("invalid username");
    }
}
