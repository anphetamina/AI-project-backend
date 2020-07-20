package it.polito.ai.backend.services.vm;

public class InvalidTotNumException extends VirtualMachineServiceConflictException {
    public InvalidTotNumException(String value, String current) {
        super(String.format("invalid total number %s, current total number of virtual machines %s", value, current));
    }
}
