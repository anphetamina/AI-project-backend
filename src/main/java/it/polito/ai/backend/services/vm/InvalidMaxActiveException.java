package it.polito.ai.backend.services.vm;

public class InvalidMaxActiveException extends VirtualMachineServiceConflictException {
    public InvalidMaxActiveException(String value, String current) {
        super(String.format("invalid max active number %s, current active virtual machines %s", value, current));
    }
}
