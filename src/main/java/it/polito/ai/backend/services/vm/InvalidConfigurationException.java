package it.polito.ai.backend.services.vm;

public class InvalidConfigurationException extends VirtualMachineServiceConflictException {
    public InvalidConfigurationException(String message) {
        super(message);
    }
}
