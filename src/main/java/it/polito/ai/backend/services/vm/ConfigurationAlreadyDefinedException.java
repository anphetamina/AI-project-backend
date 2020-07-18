package it.polito.ai.backend.services.vm;

public class ConfigurationAlreadyDefinedException extends VirtualMachineServiceConflictException {
    public ConfigurationAlreadyDefinedException(String teamId) {
        super(String.format("configuration already defined for team %s", teamId));
    }
}
