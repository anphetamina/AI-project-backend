package it.polito.ai.backend.services.vm;

public class InvalidNumVcpuException extends VirtualMachineServiceException {
    public InvalidNumVcpuException(String message) {
        super(message);
    }
}
