package it.polito.ai.backend.services.vm;

public class InvalidTotNumException extends VirtualMachineServiceException {
    public InvalidTotNumException(String message) {
        super(message);
    }
}
