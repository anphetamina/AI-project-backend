package it.polito.ai.backend.controllers;

import io.swagger.v3.oas.annotations.Operation;
import it.polito.ai.backend.services.notification.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/API/notifications")
public class NotificationController {
    @Autowired
    NotificationService notificationService;


    @Operation(summary = "confirm team return true if operation has successful else return false")
    @GetMapping("/teams/confirm/{token}")
    String confirmToken(@PathVariable String token) {

        return notificationService.confirm(token);

    }

    @Operation(summary = "reject team  return true if operation has successful else return false")
    @GetMapping("/teams/reject/{token}")
    String rejectToken(@PathVariable String token) {

        return notificationService.reject(token);
    }

}
