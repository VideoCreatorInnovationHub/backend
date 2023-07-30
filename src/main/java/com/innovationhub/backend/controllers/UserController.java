package com.innovationhub.backend.controllers;

import com.innovationhub.backend.dto.JwtRequest;
import com.innovationhub.backend.dto.JwtResponse;
import com.innovationhub.backend.models.User;
import com.innovationhub.backend.services.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Log4j2
@RestController
@AllArgsConstructor
@RequestMapping("api/auth")
public class UserController {
    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody @Valid User user) {
        userService.saveUser(user);
        return ResponseEntity.ok("Registration Success");
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@RequestBody @Valid JwtRequest authenticationRequest) {
        String token = userService.authenticate(authenticationRequest);
        return ResponseEntity.ok(new JwtResponse(token));
    }
}
