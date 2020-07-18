package it.polito.ai.backend.services.vm;

public class NumVcpuNotAvailableException extends VirtualMachineServiceConflictException {
    public NumVcpuNotAvailableException(String value, String requested, String max) {
        super(String.format("invalid num vcpu %s, requested %s exceeds the maximum %s", value, requested, max));
    }
}
