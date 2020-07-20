package it.polito.ai.backend.services.vm;

public class VirtualMachineModelAlreadyDefinedException extends VirtualMachineServiceConflictException {
    public VirtualMachineModelAlreadyDefinedException(String course) {
        super(String.format("virtual machine model already defined for course %s", course));
    }
}
