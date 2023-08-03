package com.innovationhub.backend.controllers;

import com.innovationhub.backend.dto.JwtRequest;
import com.innovationhub.backend.dto.JwtResponse;
import com.innovationhub.backend.models.User;
import com.innovationhub.backend.services.UserService;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.security.auth.login.AccountNotFoundException;
import javax.validation.Valid;

@Log4j2
@RestController
@AllArgsConstructor
@RequestMapping("api/auth")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
@OpenAPIDefinition(info = @Info(title = "User API", version = "1.0", description = "Web server for user authentication"))
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

    @GetMapping("/user_info")
    public ResponseEntity<User> login(Authentication authentication) throws AccountNotFoundException {
        return ResponseEntity.ok(userService.getUser(authentication.getName()));
    }
}
