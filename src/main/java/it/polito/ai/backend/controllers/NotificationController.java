package it.polito.ai.backend.controllers;

import it.polito.ai.backend.services.*;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequestMapping("/API/notification")
public class NotificationController {

    @Autowired
    NotificationService notificationService;
    @Autowired
    ModelMapper modelMapper;

    @GetMapping("/confirm/{token}")
    public String confirmToken(Model model, @PathVariable String token) {
        model.addAttribute("class", "alert-danger");
        try {
            if (notificationService.confirm(token)) {
                model.addAttribute("msg", "Your team is now active!");
            } else {
                model.addAttribute("msg", "Your confirmation has been registered.");
            }
            model.addAttribute("class", "alert-success");
        } catch (TokenExpiredException exception) {
            model.addAttribute("msg", "This token has expired!");
        } catch (TeamServiceException exception) {
            model.addAttribute("msg", "Token not found.");
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
        }
        return "token";
    }

    @GetMapping("/reject/{token}")
    public String rejectToken(Model model, @PathVariable String token) {
        model.addAttribute("class", "alert-danger");
        try {
            if (notificationService.reject(token)) {
                model.addAttribute("msg", "Your team has been deleted.");
            }
            model.addAttribute("class", "alert-warning");
        } catch (TokenExpiredException exception) {
            model.addAttribute("msg", "This token has expired!");
        } catch (TeamServiceException exception) {
            model.addAttribute("msg", "Token not found.");
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
        }
        return "token";
    }

}
