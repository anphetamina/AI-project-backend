package it.polito.ai.backend.services.vm;

public class InvalidMaxOnNumException extends VirtualMachineServiceException {
    public InvalidMaxOnNumException(String message) {
        super(message);
    }
}
