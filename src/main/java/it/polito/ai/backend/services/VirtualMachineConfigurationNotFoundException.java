package it.polito.ai.backend.services;

public class VirtualMachineConfigurationNotFoundException extends VirtualMachineServiceException {
    public VirtualMachineConfigurationNotFoundException(String message) {
        super(message);
    }
}
