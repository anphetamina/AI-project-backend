package it.polito.ai.backend.services.vm;

public class ConfigurationNotDefinedException extends VirtualMachineServiceException {
    public ConfigurationNotDefinedException(String message) {
        super(message);
    }
}
