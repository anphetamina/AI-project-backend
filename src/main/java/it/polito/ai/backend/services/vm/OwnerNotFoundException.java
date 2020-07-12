package it.polito.ai.backend.services.vm;

public class OwnerNotFoundException extends VirtualMachineServiceException {
    public OwnerNotFoundException(String message) {
        super(message);
    }
}
