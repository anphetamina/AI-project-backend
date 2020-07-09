package it.polito.ai.backend.services;

public class VirtualMachineModelNotFoundException extends VirtualMachineServiceException {
    public VirtualMachineModelNotFoundException(String message) {
        super(message);
    }
}
