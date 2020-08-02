package it.polito.ai.backend.controllers;

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


    @GetMapping("/teams/confirm/{token}")
    boolean confirmToken(@PathVariable String token) {
        //todo studentId è quello loggato
        return notificationService.confirm(token);

    }

    @GetMapping("/teams/reject/{token}")
    boolean rejectToken(@PathVariable String token) {
        //todo studentId è quello loggato
        return notificationService.reject(token);
    }

}
