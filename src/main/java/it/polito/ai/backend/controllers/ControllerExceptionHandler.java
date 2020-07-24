package it.polito.ai.backend.controllers;

import it.polito.ai.backend.services.exercise.AssignmentNotFoundException;
import it.polito.ai.backend.services.exercise.ExerciseNotFoundException;
import it.polito.ai.backend.services.team.TeamServiceConflictException;
import it.polito.ai.backend.services.team.TeamServiceNotFoundException;
import it.polito.ai.backend.services.vm.VirtualMachineServiceConflictException;
import it.polito.ai.backend.services.vm.VirtualMachineServiceNotFoundException;
import org.modelmapper.MappingException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@ControllerAdvice
public class ControllerExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler({TeamServiceNotFoundException.class, VirtualMachineServiceNotFoundException.class, AssignmentNotFoundException.class, ExerciseNotFoundException.class})
    void handleNotFoundException(HttpServletResponse response, Exception exception) throws IOException {
        response.sendError(HttpStatus.NOT_FOUND.value(), exception.getMessage());
    }

    @ExceptionHandler({TeamServiceConflictException.class, VirtualMachineServiceConflictException.class})
    void handleConflictException(HttpServletResponse response, Exception exception) throws IOException {
        response.sendError(HttpStatus.CONFLICT.value(), exception.getMessage());
    }

    @ExceptionHandler({IllegalArgumentException.class, ClassCastException.class, MappingException.class/*, NullPointerException.class*/})
    void handleArgumentException(HttpServletResponse response, Exception exception) throws IOException {
        response.sendError(HttpStatus.BAD_REQUEST.value());
    }
}
