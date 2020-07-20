package it.polito.ai.backend.services.vm;

public class InvalidNumVcpuException extends VirtualMachineServiceConflictException {
    public InvalidNumVcpuException(String value, String min) {
        super(String.format("invalid vcpu number %s, allowed min %s", value, min));
    }
}
