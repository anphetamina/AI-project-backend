package it.polito.ai.backend.services.vm;

public class ConfigurationAlreadyDefinedException extends VirtualMachineServiceException {
    public ConfigurationAlreadyDefinedException(String message) {
        super(message);
    }
}
