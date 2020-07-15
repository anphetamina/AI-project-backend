package it.polito.ai.backend.services.vm;

public class InvalidConfigurationException extends VirtualMachineServiceException {
    public InvalidConfigurationException(String message) {
        super(message);
    }
}
