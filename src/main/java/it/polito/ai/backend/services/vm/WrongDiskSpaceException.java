package it.polito.ai.backend.services.vm;

public class WrongDiskSpaceException extends VirtualMachineServiceException{
    public WrongDiskSpaceException(String message) {
        super(message);
    }
}
