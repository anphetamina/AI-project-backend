package it.polito.ai.backend.services.vm;

public class WrongNumVcpuException extends VirtualMachineServiceException {
    public WrongNumVcpuException(String message) {
        super(message);
    }
}
