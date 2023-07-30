package com.innovationhub.backend.services;

import com.innovationhub.backend.dto.JwtRequest;
import com.innovationhub.backend.exception.AccountInfoConflictException;
import com.innovationhub.backend.exception.AuthenticationException;
import com.innovationhub.backend.models.User;
import com.innovationhub.backend.repositories.UserRepository;
import com.innovationhub.backend.utils.JwtTokenUtil;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserService {
    private UserRepository userRepository;
    private JwtTokenUtil jwtTokenUtil;
    private AuthenticationManager authenticationManager;
    public String authenticate(JwtRequest authenticationRequest) {
        final Authentication authentication;
        try {
            authentication = generateAuthentication(authenticationRequest.getUsername(), authenticationRequest.getPassword());
        } catch (BadCredentialsException e) {
            throw new AuthenticationException("Invalid User Credentials");
        }
        return jwtTokenUtil.generateToken(authentication);
    }
    private Authentication generateAuthentication(String username, String password) {
        return authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
    }

    public void saveUser(User user) {
        if (usernameExist(user)) {
            throw new AccountInfoConflictException("Username already exist");
        } else if (emailExist(user)) {
            throw new AccountInfoConflictException("Email already been used");
        }
        userRepository.save(user);
    }
    public boolean usernameExist(User user) {
        return userRepository.findUserByUsername(user.getUsername()).isPresent();
    }
    public boolean emailExist(User user) {
        return userRepository.findUserByEmail(user.getEmail()).isPresent();
    }
}
