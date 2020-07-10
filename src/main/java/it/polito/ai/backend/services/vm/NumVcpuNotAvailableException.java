package it.polito.ai.backend.services.vm;

public class NumVcpuNotAvailableException extends VirtualMachineServiceException {
    public NumVcpuNotAvailableException(String message) {
        super(message);
    }
}
