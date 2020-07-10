package it.polito.ai.backend.services.vm;

public class TooManyVirtualMachinesException extends VirtualMachineServiceException{
    public TooManyVirtualMachinesException(String message) {
        super(message);
    }
}
