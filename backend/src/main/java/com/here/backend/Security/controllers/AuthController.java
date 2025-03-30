
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
import java.util.HashMap;
import java.util.Map;
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
            return ResponseEntity.status(403).body("Your account has been temporarily banned due to the number of failed login attempts.");
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
}

// package com.here.backend.Security.controllers;

// import jakarta.validation.Valid;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.RequestBody;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RestController;
// import java.util.Collections;
// import java.util.Map;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.security.authentication.AuthenticationManager;
// import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
// import org.springframework.security.core.Authentication;
// import org.springframework.security.core.annotation.AuthenticationPrincipal;
// import org.springframework.security.core.context.SecurityContextHolder;
// import org.springframework.security.crypto.password.PasswordEncoder;
// import org.springframework.security.oauth2.core.user.OAuth2User;
// import org.springframework.web.bind.annotation.CrossOrigin;
// import org.springframework.web.bind.annotation.GetMapping;
// import com.here.backend.Security.jwt.JwtUtils;
// import com.here.backend.Security.payload.request.LoginRequest;
// import com.here.backend.Security.payload.request.SignupRequest;
// import com.here.backend.Security.payload.response.JwtResponse;
// import com.here.backend.Security.payload.response.MessageResponse;
// import com.here.backend.Teacher.TeacherEntity;
// import com.here.backend.Teacher.TeacherRepository;
// import com.here.backend.Security.security.services.UserDetailsImpl;

// @RestController
// @RequestMapping("/api/auth")
// @CrossOrigin(origins = "*", maxAge = 3600)
// public class AuthController {
//     private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

//     @Autowired
//     AuthenticationManager authenticationManager;

//     @Autowired
//     TeacherRepository TeacherRepository;

//     @Autowired
//     PasswordEncoder encoder;

//     @Autowired
//     private JwtUtils jwtUtils;

//     @PostMapping("/signin")
//     public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

//         Authentication authentication = authenticationManager.authenticate(
//         new UsernamePasswordAuthenticationToken(loginRequest.getName(), loginRequest.getPassword()));

//         SecurityContextHolder.getContext().setAuthentication(authentication);
//         String jwt = jwtUtils.generateJwtToken(authentication);

//         UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();   

//         return ResponseEntity.ok(new JwtResponse(jwt, 
//             userDetails.getId(), 
//             userDetails.getName(), 
//             userDetails.getEmail() 
//         ));
//     }

//     @PostMapping("/signup")
//     public ResponseEntity<?> registerUserByUsername(@Valid @RequestBody SignupRequest signUpRequest) {
//         if (TeacherRepository.existsByName(signUpRequest.getName())) {
//             return ResponseEntity.badRequest().body(new MessageResponse("Error: Username is already taken!"));
//         }

//         if (TeacherRepository.existsByEmail(signUpRequest.getEmail())) {
//             return ResponseEntity.badRequest().body(new MessageResponse("Error: Email is already in use!"));
//         }

//         // Create new user's account
//         TeacherEntity user = new TeacherEntity(
//             signUpRequest.getName(),
//             signUpRequest.getEmail(),
//             encoder.encode(signUpRequest.getPassword())
//         );

//         TeacherRepository.save(user);

//         // Authenticate the user
//         Authentication authentication = authenticationManager.authenticate(
//             new UsernamePasswordAuthenticationToken(signUpRequest.getName(), signUpRequest.getPassword())
//         );

//         SecurityContextHolder.getContext().setAuthentication(authentication);
//         String jwt = jwtUtils.generateJwtToken(authentication);

//         UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

//         // Create response with token
//         JwtResponse jwtResponse = new JwtResponse(jwt, userDetails.getId(), userDetails.getName(), userDetails.getEmail());

//         return ResponseEntity.ok(jwtResponse);
//     }

//     @GetMapping("/Teacher")
//     public Map<String, Object> Teacher(@AuthenticationPrincipal OAuth2User principal) {
//         return Collections.singletonMap("name", principal.getAttribute("name"));
//     }
// }

