package it.polito.ai.backend.services.vm;

public class VirtualMachineStillActiveException extends VirtualMachineServiceConflictException {
    public VirtualMachineStillActiveException(String vmId) {
        super(String.format("virtual machine %s still active", vmId));
    }
}
