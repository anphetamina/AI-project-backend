package it.polito.ai.backend.security;

import javax.security.sasl.AuthenticationException;

public class PrincipalNotFoundException extends SecurityServiceException {
    public PrincipalNotFoundException() {
        super("user not authenticated");
    }
}
