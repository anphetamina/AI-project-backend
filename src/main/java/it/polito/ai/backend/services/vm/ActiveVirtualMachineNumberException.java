package it.polito.ai.backend.services.vm;

public class ActiveVirtualMachineNumberException extends VirtualMachineServiceConflictException {
    public ActiveVirtualMachineNumberException(String requested, String max) {
        super(String.format("request %s number of active virtual machines exceeds %s", requested, max));
    }
}
