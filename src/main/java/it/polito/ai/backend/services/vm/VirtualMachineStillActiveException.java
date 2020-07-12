package it.polito.ai.backend.services.vm;

public class VirtualMachineStillActiveException extends VirtualMachineServiceException {
    public VirtualMachineStillActiveException(String message) {
        super(message);
    }
}
