package it.polito.ai.backend.services.vm;

public class ConfigurationNotFoundException extends VirtualMachineServiceNotFoundException {
    public ConfigurationNotFoundException(String configurationId) {
        super(String.format("configuration %s not found", configurationId));
    }
}
