package it.polito.ai.backend.services.vm;

public class VirtualMachineNumberException extends VirtualMachineServiceConflictException{
    public VirtualMachineNumberException(String tot) {
        super(String.format("total number %s of virtual machines exceeds maximum", tot));
    }
}
