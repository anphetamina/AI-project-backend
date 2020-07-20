package it.polito.ai.backend.services.vm;

public class DiskSpaceNotAvailableException extends VirtualMachineServiceConflictException {
    public DiskSpaceNotAvailableException(String value, String requested, String max) {
        super(String.format("invalid disk space %s, requested %s exceeds the maximum %s", value, requested, max));
    }
}
