package it.polito.ai.backend.services.vm;

public class VirtualMachineServiceConflictException extends VirtualMachineServiceException {
    public VirtualMachineServiceConflictException(String message) {
        super(message);
    }
}
