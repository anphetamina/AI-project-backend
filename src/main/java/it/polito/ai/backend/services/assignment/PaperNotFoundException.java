package it.polito.ai.backend.services.assignment;

public class PaperNotFoundException extends AssignmentServiceException {
    public PaperNotFoundException(String message) {
        super(message);
    }
}
