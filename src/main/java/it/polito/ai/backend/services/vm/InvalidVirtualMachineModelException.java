package it.polito.ai.backend.services.vm;

public class InvalidVirtualMachineModelException extends VirtualMachineServiceException {
    public InvalidVirtualMachineModelException(String message) {
        super(message);
    }
}
