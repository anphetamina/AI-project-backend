package it.polito.ai.backend.services.vm;

public class ConfigurationNotDefinedException extends VirtualMachineServiceNotFoundException {
    public ConfigurationNotDefinedException(String teamId) {
        super(String.format("virtual machine configuration not defined for team %s", teamId));
    }
}
