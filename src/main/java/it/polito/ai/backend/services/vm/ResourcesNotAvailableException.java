package it.polito.ai.backend.services.vm;

public class ResourcesNotAvailableException extends VirtualMachineServiceException {
    public ResourcesNotAvailableException(String message) {
        super(message);
    }
}
