package it.polito.ai.backend.services.vm;

public class DiskSpaceNotAvailableException extends VirtualMachineServiceException {
    public DiskSpaceNotAvailableException(String message) {
        super(message);
    }
}
