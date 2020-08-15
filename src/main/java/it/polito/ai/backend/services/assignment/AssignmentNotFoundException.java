package it.polito.ai.backend.services.assignment;

public class AssignmentNotFoundException extends AssignmentServiceException {
    public AssignmentNotFoundException(String message) {
        super(message);
    }
}
