package it.polito.ai.backend.services.vm;

public class TooManyOnVirtualMachinesException extends VirtualMachineServiceException {
    public TooManyOnVirtualMachinesException(String message) {
        super(message);
    }
}
