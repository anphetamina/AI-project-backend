package it.polito.ai.backend.controllers;

import it.polito.ai.backend.services.exercise.AssignmentNotFoundException;
import it.polito.ai.backend.services.exercise.ExerciseNotFoundException;
import it.polito.ai.backend.services.team.TeamServiceBadRequestException;
import it.polito.ai.backend.services.team.TeamServiceConflictException;
import it.polito.ai.backend.services.team.TeamServiceNotFoundException;
import it.polito.ai.backend.services.vm.VirtualMachineServiceBadRequestException;
import it.polito.ai.backend.services.vm.VirtualMachineServiceConflictException;
import it.polito.ai.backend.services.vm.VirtualMachineServiceNotFoundException;
import org.modelmapper.MappingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestControllerAdvice
public class ControllerExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler({TeamServiceNotFoundException.class,
            VirtualMachineServiceNotFoundException.class,
            AssignmentNotFoundException.class,
            ExerciseNotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    ResponseEntity<String> handleNotFoundException(HttpServletResponse response, Exception exception, RuntimeException runtimeException) throws IOException {
        // response.sendError(HttpStatus.NOT_FOUND.value(), exception.getMessage());
        return new ResponseEntity<>(runtimeException.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({TeamServiceConflictException.class, VirtualMachineServiceConflictException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    ResponseEntity<String> handleConflictException(HttpServletResponse response, Exception exception, RuntimeException runtimeException) throws IOException {
        // response.sendError(HttpStatus.CONFLICT.value(), exception.getMessage());
        return new ResponseEntity<>(runtimeException.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler({VirtualMachineServiceBadRequestException.class,
            TeamServiceBadRequestException.class,
            IllegalArgumentException.class,
            ClassCastException.class,
            MappingException.class/*, NullPointerException.class*/})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ResponseEntity<String> handleArgumentException(HttpServletResponse response, Exception exception, RuntimeException runtimeException) throws IOException {
        // response.sendError(HttpStatus.BAD_REQUEST.value());
        return new ResponseEntity<>(runtimeException.getMessage(), HttpStatus.BAD_REQUEST);
    }
}
