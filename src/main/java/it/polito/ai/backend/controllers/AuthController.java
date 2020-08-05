package it.polito.ai.backend.controllers;

import it.polito.ai.backend.dtos.AuthenticationRequest;
import it.polito.ai.backend.dtos.UserInformationRequest;
import it.polito.ai.backend.security.CustomUserDetailsService;
import it.polito.ai.backend.security.JwtTokenProvider;
import it.polito.ai.backend.repositories.UserRepository;

import it.polito.ai.backend.services.Utils;
import org.apache.tika.exception.TikaException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @Autowired
    CustomUserDetailsService customUserDetailsService;

    @PostMapping("/sign-in")
    public ResponseEntity signIn(@RequestBody @Valid AuthenticationRequest data){
        try {

            String username = customUserDetailsService.getId(data.getUsername());
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username,data.getPassword()));
            String token = jwtTokenProvider.createToken(username,
                   customUserDetailsService.getRoles(username));
            Map<Object,Object> model = new HashMap<>();
            model.put("username",username);
            model.put("token",token);
            return ok(model);
        }catch (AuthenticationException e){
            throw new BadCredentialsException("Invalid username/password supplied");
        }
    }

    @PostMapping("/sign-up")
    public ResponseEntity.BodyBuilder signUp(@RequestPart("user") @Valid UserInformationRequest user, @RequestPart("image")MultipartFile file) {
        try {
            Utils.checkTypeImage(file);
            customUserDetailsService.signUpUser(user,Utils.getBytes(file));
            return ResponseEntity.status(HttpStatus.OK);

        } catch (AuthenticationException e) {
            throw new BadCredentialsException("Invalid username/password supplied");
        } catch (TikaException | IOException e) {
            e.printStackTrace();
            throw  new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "invalid file content");
        }
    }

    @GetMapping("/sign-up/confirm/{token}")
    boolean confirmMail(@PathVariable String token) {
        return customUserDetailsService.confirmUser(token);
    }

    @GetMapping("/sing-out")
    boolean longOut(HttpServletRequest request){
        String token =jwtTokenProvider.resolveToken(request);
        return jwtTokenProvider.revokeToken(token);
    }

}
