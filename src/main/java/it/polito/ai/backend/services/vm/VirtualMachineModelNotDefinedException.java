package it.polito.ai.backend.services.vm;

public class VirtualMachineModelNotDefinedException extends VirtualMachineServiceNotFoundException {
    public VirtualMachineModelNotDefinedException(String message) {
        super(message);
    }
}
