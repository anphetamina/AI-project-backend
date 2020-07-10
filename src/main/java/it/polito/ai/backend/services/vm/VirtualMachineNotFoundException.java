package it.polito.ai.backend.services.vm;

public class VirtualMachineNotFoundException extends VirtualMachineServiceException {
    public VirtualMachineNotFoundException(String message) {
        super(message);
    }
}
