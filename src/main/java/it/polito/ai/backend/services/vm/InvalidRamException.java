package it.polito.ai.backend.services.vm;

public class InvalidRamException extends VirtualMachineServiceException {
    public InvalidRamException(String message) {
        super(message);
    }
}
