package it.polito.ai.backend.services.vm;

public class ConfigurationNotFoundException extends VirtualMachineServiceException {
    public ConfigurationNotFoundException(String message) {
        super(message);
    }
}
