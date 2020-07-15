package it.polito.ai.backend.services.vm;

public class VirtualMachineModelAlreadyDefinedException extends VirtualMachineServiceException {
    public VirtualMachineModelAlreadyDefinedException(String message) {
        super(message);
    }
}
