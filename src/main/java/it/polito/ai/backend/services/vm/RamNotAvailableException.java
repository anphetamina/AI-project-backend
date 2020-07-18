package it.polito.ai.backend.services.vm;

public class RamNotAvailableException extends VirtualMachineServiceConflictException {
    public RamNotAvailableException(String value, String requested, String max) {
        super(String.format("invalid ram %s, requested %s exceeds the maximum %s", value, requested, max));
    }
}
