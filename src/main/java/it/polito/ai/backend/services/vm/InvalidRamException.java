package it.polito.ai.backend.services.vm;

public class InvalidRamException extends VirtualMachineServiceConflictException {
    public InvalidRamException(String value, String min) {
        super(String.format("invalid ram %s, allowed min %s", value, min));
    }
}
