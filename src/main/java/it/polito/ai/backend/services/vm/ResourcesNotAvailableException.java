package it.polito.ai.backend.services.vm;

import it.polito.ai.backend.services.vm.VirtualMachineServiceException;

public class ResourcesNotAvailableException extends VirtualMachineServiceException {
    public ResourcesNotAvailableException(String message) {
        super(message);
    }
}
