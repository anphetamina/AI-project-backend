package it.polito.ai.backend.services.vm;

public class InvalidDiskSpaceException extends VirtualMachineServiceException{
    public InvalidDiskSpaceException(String message) {
        super(message);
    }
}
