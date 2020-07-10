package it.polito.ai.backend.services.vm;

public class VirtualMachineModelNotFoundException extends VirtualMachineServiceException {
    public VirtualMachineModelNotFoundException(String message) {
        super(message);
    }
}
