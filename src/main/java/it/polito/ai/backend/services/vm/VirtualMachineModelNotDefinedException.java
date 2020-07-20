package it.polito.ai.backend.services.vm;

public class VirtualMachineModelNotDefinedException extends VirtualMachineServiceNotFoundException {
    public VirtualMachineModelNotDefinedException(String course) {
        super(String.format("virtual machine model not defined for course %s", course));
    }
}
