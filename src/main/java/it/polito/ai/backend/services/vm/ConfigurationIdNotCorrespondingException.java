package it.polito.ai.backend.services.vm;

public class ConfigurationIdNotCorrespondingException extends VirtualMachineServiceBadRequestException {
    public ConfigurationIdNotCorrespondingException(String actual, String expected) {
        super(String.format("provided %s id does not match with %s", actual, expected));
    }
}
