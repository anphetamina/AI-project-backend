package it.polito.ai.backend.services.vm;

public class InvalidVirtualMachineModelException extends VirtualMachineServiceConflictException {
    public InvalidVirtualMachineModelException(String modelId) {
        super(String.format("invalid virtual machine model %s", modelId));
    }
}
