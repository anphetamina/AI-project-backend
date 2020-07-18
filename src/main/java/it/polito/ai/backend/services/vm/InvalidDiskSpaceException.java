package it.polito.ai.backend.services.vm;

public class InvalidDiskSpaceException extends VirtualMachineServiceConflictException {
    public InvalidDiskSpaceException(String value, String min) {
        super(String.format("invalid disk space %s, allowed min %s", value, min));
    }
}
