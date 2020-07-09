package it.polito.ai.backend.services;

public class VirtualMachineNotFoundException extends VirtualMachineServiceException {
    public VirtualMachineNotFoundException(String message) {
        super(message);
    }
}
