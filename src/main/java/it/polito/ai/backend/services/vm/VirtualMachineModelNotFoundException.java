package it.polito.ai.backend.services.vm;

public class VirtualMachineModelNotFoundException extends VirtualMachineServiceNotFoundException {
    public VirtualMachineModelNotFoundException(String id) {
        super(String.format("virtual machine model %s not found", id));
    }
}
