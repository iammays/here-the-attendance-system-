package com.here.backend.Security.controllers;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.here.backend.Security.jwt.JwtUtils;
import com.here.backend.Security.payload.request.LoginRequest;
import com.here.backend.Security.payload.response.JwtResponse;
import com.here.backend.Teacher.TeacherRepository;
import com.here.backend.Security.security.services.UserDetailsImpl;
import com.here.backend.Emails.EmailSenderService;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private static final int MAX_FAILED_ATTEMPTS = 3;
    private Map<String, Integer> failedAttempts = new ConcurrentHashMap<>();
    
    @Autowired
    AuthenticationManager authenticationManager;
    
    @Autowired
    TeacherRepository teacherRepository;
    
    @Autowired
    PasswordEncoder encoder;
    
    @Autowired
    private JwtUtils jwtUtils;
    
    @Autowired
    private EmailSenderService emailSenderService;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        String username = loginRequest.getName();
        
        if (failedAttempts.getOrDefault(username, 0) >= MAX_FAILED_ATTEMPTS) {
            return ResponseEntity.status(403).body("Your account has been temporarily banned due to multiple failed login attempts. You entered the wrong password 3 times. Your account will be banned for 10 minutes before you can try again.");
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, loginRequest.getPassword())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

            failedAttempts.remove(username);
            
            return ResponseEntity.ok(new JwtResponse(jwt, userDetails.getId(), userDetails.getName(), userDetails.getEmail()));
        } catch (BadCredentialsException e) {
            failedAttempts.put(username, failedAttempts.getOrDefault(username, 0) + 1);
            
            if (failedAttempts.get(username) >= MAX_FAILED_ATTEMPTS) {
                var teacher = teacherRepository.findByName(username);
                
                if (teacher.isPresent()) {
                    String userEmail = teacher.get().getEmail();
                    emailSenderService.sendSimpleEmail(userEmail, "Failed login attempts", 
                        "Your password was entered incorrectly 3 times. If it wasn't you, please secure your account.");
                }
            }            
            
            return ResponseEntity.status(401).body("Incorrect username or password.");
        }
    }

    @GetMapping("/logout")
    public ResponseEntity<?> logoutUser() {
        
// Invalidate the JWT token or perform any other logout logic here
        return ResponseEntity.ok("User logged out successfully!");
    }
}