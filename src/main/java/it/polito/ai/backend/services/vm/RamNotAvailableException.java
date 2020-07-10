package it.polito.ai.backend.services.vm;

public class RamNotAvailableException extends VirtualMachineServiceException {
    public RamNotAvailableException(String message) {
        super(message);
    }
}
