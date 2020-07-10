package it.polito.ai.backend.services.vm;

public class VirtualMachineConfigurationNotFoundException extends VirtualMachineServiceException {
    public VirtualMachineConfigurationNotFoundException(String message) {
        super(message);
    }
}
