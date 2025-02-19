package com.shouq.project.Security.controllers;

import jakarta.validation.Valid;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.security.SecureRandom;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;

import com.shouq.project.Security.payload.request.LoginRequest;
import com.shouq.project.Security.payload.request.SignupRequest;
import com.shouq.project.Security.payload.response.JwtResponse;
import com.shouq.project.Security.payload.response.MessageResponse;
import com.shouq.project.users.User;
import com.shouq.project.users.UserRepository;


import com.shouq.project.Security.security.services.UserDetailsImpl;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
public class AuthController {
  @Autowired
  AuthenticationManager authenticationManager;

  @Autowired
  UserRepository userRepository;

  @Autowired
  PasswordEncoder encoder;

  @Autowired
  com.shouq.project.Security.jwt.JwtUtils jwtUtils;

  @PostMapping("/api/auth/signin")
  public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

    Authentication authentication = authenticationManager.authenticate(
      new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

    SecurityContextHolder.getContext().setAuthentication(authentication);
    String jwt = jwtUtils.generateJwtToken(authentication);

    UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();   

    return ResponseEntity.ok(new JwtResponse(jwt, 
      userDetails.getId(), 
        userDetails.getUsername(), 
        userDetails.getEmail() 
      ));
  }
  


  @PostMapping("/api/auth/signup")
  public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
    if (userRepository.existsByUsername(signUpRequest.getUsername())) {
        return ResponseEntity.badRequest().body(new MessageResponse("Error: Username is already taken!"));
    }

    if (userRepository.existsByEmail(signUpRequest.getEmail())) {
        return ResponseEntity.badRequest().body(new MessageResponse("Error: Email is already in use!"));
    }

        // Create new user's account
        User user = new User(
                signUpRequest.getUsername(),
                signUpRequest.getEmail(),
                encoder.encode(signUpRequest.getPassword()),
                signUpRequest.getProfilePic(),
                signUpRequest.getBio(),
                signUpRequest.getGender(),
                signUpRequest.getDOB(),
                signUpRequest.getLocation());

        userRepository.save(user);

        // Authenticate the user
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(signUpRequest.getUsername(), signUpRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // Create response with token
        JwtResponse jwtResponse = new JwtResponse(jwt, userDetails.getId(), userDetails.getUsername(), userDetails.getEmail());

        return ResponseEntity.ok(jwtResponse);
}


  private static final String CLIENT_ID = "44788364330-kadh6flrqpcik0ou46s1iuuf86hncv1u.apps.googleusercontent.com";
    private static final String JWT_SECRET = "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";












    // @PostMapping("/api/auth/google")
    // @CrossOrigin(origins = "http://localhost:3000")
    // public ResponseEntity<?> getLoginInfo(@RequestHeader("Authorization") String authorization, @RequestBody Map<String, String> userRequest) {
    //     try {
    //         String token = authorization.replace("Bearer ", "");
    //         String url = "https://www.googleapis.com/oauth2/v3/tokeninfo?access_token=" + token;
    
    //         RestTemplate restTemplate = new RestTemplate();
    //         Map<String, Object> userInfo = restTemplate.getForObject(url, Map.class);
    
    //         if (userInfo == null || userInfo.containsKey("error")) {
    //             return ResponseEntity.badRequest().body("Invalid Google token");
    //         }
    
    //         String email = userRequest.get("email");
    //         String username = userRequest.get("username");
    
    //         Optional<User> optionalUser = Optional.of(userRepository.findByEmail(email));
    //         System.out.println(email + " " + username);
    
    //         String jwtToken = jwtUtils.generateTokenFromEmail(email);
    //         if (optionalUser.isPresent()) {
    //             User user = optionalUser.get();
    //             userInfo.put("id", user.getUserID());
    //             userInfo.put("username", user.getUsername());
    //             userInfo.put("accessToken", jwtToken);
    //         } else {
    //             // Create a new user using the registerUser method
    //             SignupRequest signUpRequest = new SignupRequest();
    //             signUpRequest.setEmail(email);
    //             signUpRequest.setUsername(username);
    //             // signUpRequest.setPassword(generateRandomPassword()); // Implement this method to generate a random password
    //             // signUpRequest.setProfilePic(""); // Set profile pic to empty for now
    //             // signUpRequest.setBio(""); // Set bio to empty for now
    //             // signUpRequest.setLocation(""); // Set location to empty for now
    
    //             ResponseEntity<?> registerResponse = registerUser(signUpRequest);
    //             if (registerResponse.getStatusCode().is2xxSuccessful()) {
    //                 // User registered successfully, update userInfo
    //                 User savedUser = userRepository.findByEmail(email);
    //                 userInfo.put("id", savedUser.getUserID());
    //                 userInfo.put("username", savedUser.getUsername());
    //                 userInfo.put("accessToken", jwtToken);
    //             } else {
    //                 // Failed to register user
    //                 return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to register user");
    //             }
    //         }
    
    //         return ResponseEntity.ok(userInfo);
    //     } catch (Exception e) {
    //         e.printStackTrace();
    //         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
    //     }
    // }
    




private String generateRandomPassword() {
    String CHAR_LOWER = "abcdefghijklmnopqrstuvwxyz";
    String CHAR_UPPER = CHAR_LOWER.toUpperCase();
    String NUMBER = "0123456789";
    String OTHER_CHAR = "!@#$%&*()_+-=[]?";

    String PASSWORD_ALLOW_BASE = CHAR_LOWER + CHAR_UPPER + NUMBER + OTHER_CHAR;
    SecureRandom random = new SecureRandom();
    StringBuilder password = new StringBuilder(8);

    // generate a random password
    for (int i = 0; i < 8; i++) {
        int index = random.nextInt(PASSWORD_ALLOW_BASE.length());
        password.append(PASSWORD_ALLOW_BASE.charAt(index));
    }

    return password.toString();
}








// takes the username token email and generate jwt and return jwt email username only if the user is registered prevousily
// @PostMapping("api/auth/google")
// @CrossOrigin(origins = "http://localhost:3000")
// public ResponseEntity<?> getLoginInfo(@RequestHeader("Authorization") String authorization, @RequestBody Map<String, String> userRequest) {
//     try {
//         String token = authorization.replace("Bearer ", "");
//         String url = "https://www.googleapis.com/oauth2/v3/tokeninfo?access_token=" + token;

//         RestTemplate restTemplate = new RestTemplate();
//         Map<String, Object> userInfo = restTemplate.getForObject(url, Map.class);
// System.out.println(userInfo);
//         if (userInfo == null || userInfo.containsKey("error")) {
//             return ResponseEntity.badRequest().body("Invalid Google token");
//         }
//         String email = userRequest.get("email");
//         String username = userRequest.get("username");
        
//         System.out.println( email + " "+ username);

//         Optional<User> optionalUser = Optional.of(userRepository.findByEmail(email));

//         String jwtToken = jwtUtils.generateTokenFromEmail(email);
//         if (optionalUser.isPresent()) {
//             User user = optionalUser.get();
//             userInfo.put("id", user.getUserID());
//             userInfo.put("username", user.getUsername());
//             userInfo.put("accessToken", jwtToken);
//         } else {
//             User newUser = new User();
//             newUser.setUsername(username);
//             newUser.setemail(email);

//             User savedUser = userRepository.save(newUser);
//             userInfo.put("id", savedUser.getUserID());
//             userInfo.put("username", savedUser.getUsername());
//             userInfo.put("accessToken", jwtToken);
//         }

//         return ResponseEntity.ok(userInfo);
//     } catch (Exception e) {
//         e.printStackTrace();
//         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
//     }
// }

@PostMapping("/api/auth/google")
    @CrossOrigin(origins = "http://localhost:3000")
    public ResponseEntity<?> getLoginInfo(@RequestHeader("Authorization") String authorization, @RequestBody Map<String, String> userRequest) {
    try {
        String token = authorization.replace("Bearer ", "");
        String url = "https://www.googleapis.com/oauth2/v3/tokeninfo?access_token=" + token;

        RestTemplate restTemplate = new RestTemplate();
        Map<String, Object> userInfo = restTemplate.getForObject(url, Map.class);

        if (userInfo == null || userInfo.containsKey("error")) {
            return ResponseEntity.badRequest().body("Invalid Google token");
        }
        String email = userRequest.get("email");
        String username = userRequest.get("username");
        Optional<User> optionalUser = Optional.ofNullable(userRepository.findByEmail(email));
        System.out.println( email + " "+ username);

        // String jwtToken = jwtUtils.generateTokenFromEmail(email);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            userInfo.put("id", user.getUserID());
            userInfo.put("username", user.getUsername());
            String jwtToken = jwtUtils.generateTokenFromUsername(user.getUsername());
            userInfo.put("accessToken", jwtToken);
        } else {
            User newUser = new User();
            newUser.setUsername(username);
            newUser.setemail(email);
            User savedUser = userRepository.save(newUser);
            userInfo.put("id", savedUser.getUserID());
            userInfo.put("username", savedUser.getUsername());
            String jwtToken = jwtUtils.generateTokenFromUsername(savedUser.getUsername());
            userInfo.put("accessToken", jwtToken);
        }

        return ResponseEntity.ok(userInfo);
    } catch (Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + e.getMessage());
    }
}






      @GetMapping("/user")
    public Map<String, Object> user(@AuthenticationPrincipal OAuth2User principal) {
        return Collections.singletonMap("name", principal.getAttribute("name"));
}
}


















    // @PostMapping("/api/auth/google")
    // public ResponseEntity<?> authenticateWithGoogle(@RequestBody Map<String, String> request) {
    //     String idTokenString = request.get("idToken");

    //     try {
    //         GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new JacksonFactory())
    //                 .setAudience(Collections.singletonList(CLIENT_ID))
    //                 .build();

    //         GoogleIdToken idToken = verifier.verify(idTokenString);
    //         if (idToken != null) {
    //             String email = idToken.getPayload().getEmail();

    //             // Create JWT token
    //             String jwtToken = Jwts.builder()
    //                     .setSubject(email)
    //                     .setIssuedAt(new Date())
    //                     .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24)) // 1 day
    //                     .signWith(SignatureAlgorithm.HS256, JWT_SECRET)
    //                     .compact();

    //             // Return token and email
    //             return ResponseEntity.ok().body(Map.of("token", jwtToken, "email", email));
    //         } else {
    //             return ResponseEntity.badRequest().body(Map.of("error", "Invalid ID token"));
    //         }
    //     } catch (GeneralSecurityException | IOException e) {
    //         e.printStackTrace();
    //         return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
    //     }
    // }


    // @CrossOrigin(origins = "http://localhost:3000")
    // @GetMapping("/api/auth/google")
    // public Map<String, String> getLoginInfo(OAuth2AuthenticationToken authentication) {
    //     Map<String, String> userInfo = new HashMap<>(); // Correct import for HashMap
    //     userInfo.put("username", authentication.getName()); // Get the username
    //     userInfo.put("email", authentication.getPrincipal().getAttributes().get("email").toString()); // Get the email
    //     // Add more user information as needed
        
    //     return userInfo;
    // }

// @GetMapping("/api/auth/google")
// @CrossOrigin(origins = "http://localhost:3000")
// public ResponseEntity<?> getLoginInfo(@RequestHeader("Authorization") String authorization) {
//     String token = authorization.replace("Bearer ", "");
//     String url = "https://www.googleapis.com/oauth2/v3/tokeninfo?access_token=" + token;

//     RestTemplate restTemplate = new RestTemplate();
//     Map<String, Object> userInfo = restTemplate.getForObject(url, Map.class);

//     if (userInfo == null || userInfo.containsKey("error")) {
//         return ResponseEntity.badRequest().body("Invalid Google token");
//     }

//     String emaill = (String) userInfo.get("email");
//     Optional<User> optionalUser = userRepository.findByEmaill(emaill);
//     if (!optionalUser.isPresent()) {
//         return ResponseEntity.badRequest().body("User not found");
//     }

//     User user = optionalUser.get();
//     userInfo.put("userId", user.getUserID());

//     return ResponseEntity.ok(userInfo);
// }

// @GetMapping("api/auth/google")
//     @CrossOrigin(origins = "http://localhost:3000")
//     public ResponseEntity<?> getLoginInfo(@RequestHeader("Authorization") String authorization) {
//         String token = authorization.replace("Bearer ", "");
//         String url = "https://www.googleapis.com/oauth2/v3/tokeninfo?access_token=" + token;

//         RestTemplate restTemplate = new RestTemplate();
//         Map<String, Object> userInfo = restTemplate.getForObject(url, Map.class);

//         if (userInfo == null || userInfo.containsKey("error")) {
//             return ResponseEntity.badRequest().body("Invalid Google token");
//         }

//         String email = (String) userInfo.get("email");
//         Optional<User> optionalUser = Optional.of(userRepository.findByEmail(email));
//         String jwtToken = jwtUtils.generateTokenFromEmail(email);
//         if (optionalUser.isPresent()) {
//             // User found, generate JWT token
//             User user = optionalUser.get();
//             userInfo.put("id", user.getUserID());
//             userInfo.put("username", user.getUsername());
//             userInfo.put("accessToken", jwtToken);
//         } else {
//             // User not found, create new user
//             String username = (String) userInfo.get("name");
//             User newUser = new User();
//             newUser.setUsername(username);
//             newUser.setemail(email);

//             User savedUser = userRepository.save(newUser);
//             userInfo.put("id", savedUser.getUserID());
//             userInfo.put("username", savedUser.getUsername());
//             userInfo.put("accessToken", jwtToken);
//         }

//         return ResponseEntity.ok(userInfo);
//     }



// @CrossOrigin(origins = "http://localhost:3000")
// @PostMapping("api/auth/google")
// public ResponseEntity<?> getLoginInfo(
//         @RequestHeader("Authorization") String authorization, 
//         @RequestBody Map<String, String> userInfoFromRequest) {
    
//     String token = authorization.replace("Bearer ", "");
//     String url = "https://www.googleapis.com/oauth2/v3/tokeninfo?access_token=" + token;

//     RestTemplate restTemplate = new RestTemplate();
//     Map<String, Object> userInfoFromGoogle = restTemplate.getForObject(url, Map.class);

//     if (userInfoFromGoogle == null || userInfoFromGoogle.containsKey("error")) {
//         return ResponseEntity.badRequest().body("Invalid Google token");
//     }

//     String email = userInfoFromRequest.get("email");
//     String username = userInfoFromRequest.get("username");

//     Optional<User> optionalUser = Optional.of(userRepository.findByEmail(email));
//     String jwtToken = jwtUtils.generateTokenFromEmail(email);

//     if (optionalUser.isPresent()) {
//         // User found, generate JWT token
//         User user = optionalUser.get();
//         userInfoFromGoogle.put("id", user.getUserID());
//         userInfoFromGoogle.put("username", user.getUsername());
//         userInfoFromGoogle.put("accessToken", jwtToken);
//     } else {
//         // User not found, create new user
//         User newUser = new User();
//         newUser.setUsername(username);
//         newUser.setemail(email);

//         User savedUser = userRepository.save(newUser);
//         userInfoFromGoogle.put("id", savedUser.getUserID());
//         userInfoFromGoogle.put("username", savedUser.getUsername());
//         userInfoFromGoogle.put("accessToken", jwtToken);
//     }

//     return ResponseEntity.ok(userInfoFromGoogle);
// }



// @GetMapping("/api/auth/google")
// @CrossOrigin(origins = "http://localhost:3000")
// public ResponseEntity<?> getLoginInfo(@RequestHeader("Authorization") String authorization) {
//     String token = authorization.replace("Bearer ", "");
//     String url = "https://www.googleapis.com/oauth2/v3/tokeninfo?access_token=" + token;

//     RestTemplate restTemplate = new RestTemplate();
//     Map<String, Object> userInfo = restTemplate.getForObject(url, Map.class);

//     if (userInfo == null || userInfo.containsKey("error")) {
//         return ResponseEntity.badRequest().body("Invalid Google token");
//     }

//     String email = (String) userInfo.get("email");
//     Optional<User> optionalUser = UserRepository.findByEmaill(email);

//     if (optionalUser.isPresent()) {
//         // User found, generate JWT token
//         User user = optionalUser.get();
//         String jwtToken = jwtUtils.generateTokenFromEmaill(email);
//         userInfo.put("id", user.getUserID());
//         userInfo.put("username", user.getUsername());
//         userInfo.put("accessToken", jwtToken);
//     } else {
//         // User not found, create new user
//         String username = (String) userInfo.get("name");
//         User newUser = new User(email, username);
//         User savedUser = userRepository.save(newUser);
//         String jwtToken = jwtUtils.generateTokenFromEmaill(email);
//         userInfo.put("id", savedUser.getUserID());
//         userInfo.put("username", savedUser.getUsername());
//         userInfo.put("accessToken", jwtToken);
//     }

//     return ResponseEntity.ok(userInfo);
// }

// @GetMapping("/api/auth/google")
// @CrossOrigin(origins = "http://localhost:3000")
// public ResponseEntity<?> getLoginInfo(@RequestHeader("Authorization") String authorization) {
//     String token = authorization.replace("Bearer ", "");
//     String url = "https://www.googleapis.com/oauth2/v3/tokeninfo?access_token=" + token;

//     RestTemplate restTemplate = new RestTemplate();
//     Map<String, Object> userInfo = restTemplate.getForObject(url, Map.class);

//     if (userInfo == null || userInfo.containsKey("error")) {
//         return ResponseEntity.badRequest().body("Invalid Google token");
//     }

//     String email = (String) userInfo.get("email");
//     Optional<User> optionalUser = userRepository.findByEmaill(email);

//     if (optionalUser.isPresent()) {
//         // User found, generate JWT token
//         User user = optionalUser.get();
//         String jwtToken = jwtUtils.generateTokenFromEmaill(SecurityContextHolder.getContext().getAuthentication());
//         userInfo.put("id", user.getUserID());
//         userInfo.put("username", user.getUsername());
//         userInfo.put("accessToken", jwtToken);
//     } else {
//         // User not found, create new user
//         String username = (String) userInfo.get("name");
//         User newUser = new User(email, username);
//         User savedUser = userRepository.save(newUser);
//         String jwtToken = jwtUtils.generateTokenFromEmaill(SecurityContextHolder.getContext().getAuthentication());
//         userInfo.put("id", savedUser.getUserID());
//         userInfo.put("username", savedUser.getUsername());
//         userInfo.put("accessToken", jwtToken);
//     }

//     return ResponseEntity.ok(userInfo);
// }