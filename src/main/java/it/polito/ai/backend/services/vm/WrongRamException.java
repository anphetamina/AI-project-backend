package it.polito.ai.backend.services.vm;

public class WrongRamException extends VirtualMachineServiceException {
    public WrongRamException(String message) {
        super(message);
    }
}
