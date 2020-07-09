package it.polito.ai.backend.services;

public class ResourcesNotAvailableException extends VirtualMachineServiceException {
    public ResourcesNotAvailableException(String message) {
        super(message);
    }
}
