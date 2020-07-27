package it.polito.ai.backend.services.vm;

public class VirtualMachineIdNotCorrespondingException extends VirtualMachineServiceBadRequestException {
    public VirtualMachineIdNotCorrespondingException(String actual, String expected) {
        super(String.format("provided %s id does not match with %s", actual, expected));
    }
}
