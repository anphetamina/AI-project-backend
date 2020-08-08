package it.polito.ai.backend.controllers;

import it.polito.ai.backend.security.DuplicateConfirmationToken;
import it.polito.ai.backend.security.InvalidJwtAuthenticationException;
import it.polito.ai.backend.security.InvalidUsernameException;
import it.polito.ai.backend.security.SecurityServiceException;
import it.polito.ai.backend.services.exercise.AssignmentNotFoundException;
import it.polito.ai.backend.services.exercise.ExerciseNotFoundException;
import it.polito.ai.backend.services.exercise.InvalidScore;
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

import javax.validation.ValidationException;

@RestControllerAdvice
public class ControllerExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler({TeamServiceNotFoundException.class,
            VirtualMachineServiceNotFoundException.class,
            AssignmentNotFoundException.class,
            ExerciseNotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    ResponseEntity<String> handleNotFoundException(RuntimeException runtimeException) {
        return new ResponseEntity<>(runtimeException.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({TeamServiceConflictException.class,
            VirtualMachineServiceConflictException.class,
            DuplicateConfirmationToken.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    ResponseEntity<String> handleConflictException(RuntimeException runtimeException) {
        return new ResponseEntity<>(runtimeException.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler({VirtualMachineServiceBadRequestException.class,
            TeamServiceBadRequestException.class,
            IllegalArgumentException.class,
            ClassCastException.class,
            MappingException.class,
            ValidationException.class,
            InvalidScore.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ResponseEntity<String> handleArgumentException(RuntimeException runtimeException) {
        return new ResponseEntity<>(runtimeException.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({SecurityServiceException.class,
            InvalidUsernameException.class,})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    ResponseEntity<String> handleAuthException(RuntimeException runtimeException) {
        return new ResponseEntity<>(runtimeException.getMessage(), HttpStatus.UNAUTHORIZED);
    }
}
