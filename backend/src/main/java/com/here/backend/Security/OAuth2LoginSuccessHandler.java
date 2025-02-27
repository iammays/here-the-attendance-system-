// package com.here.backend.Security;

// import jakarta.servlet.ServletException;
// import jakarta.servlet.http.Cookie;
// import jakarta.servlet.http.HttpServletRequest;
// import jakarta.servlet.http.HttpServletResponse;
// import lombok.RequiredArgsConstructor;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.security.core.Authentication;
// import org.springframework.security.core.context.SecurityContextHolder;
// import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
// import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
// import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
// import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
// import org.springframework.stereotype.Component;
// import com.here.backend.Teacher.TeacherEntity;
// import com.here.backend.Teacher.TeacherRepository;
// import com.here.backend.Security.jwt.JwtUtils;

// import java.io.IOException;
// import java.util.Map;

// @Component
// @RequiredArgsConstructor
// public class OAuth2LoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

//     private final TeacherRepository userRepository;
//     private final JwtUtils jwtUtils;

//     @Value("${frontend.url}")
//     private String frontendUrl;

//     @Override
//     public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws ServletException, IOException {
//         OAuth2AuthenticationToken oAuth2AuthenticationToken = (OAuth2AuthenticationToken) authentication;
//         String registrationId = oAuth2AuthenticationToken.getAuthorizedClientRegistrationId();
//         DefaultOAuth2User principal = (DefaultOAuth2User) authentication.getPrincipal();
//         Map<String, Object> attributes = principal.getAttributes();
//         String username;
    
//         if ("github".equals(registrationId)) {
//             username = attributes.getOrDefault("login", "").toString();
//         } else if ("google".equals(registrationId)) {
//             username = attributes.getOrDefault("email", "").toString();
//         } else {
//             super.onAuthenticationSuccess(request, response, authentication);
//             return;
//         }
    
//         TeacherEntity Teacher = userRepository.findByUsername(username)
//                 .orElseGet(() -> {
//                     TeacherEntity newUser = new TeacherEntity();
//                     newUser.setName(username);
//                     userRepository.save(newUser);
//                     return newUser;
//                 });
    
//         String jwt = jwtUtils.generateTokenFromUsername(username);
    
//         response.setContentType("application/json");
//         response.setCharacterEncoding("UTF-8");
    
//         Cookie tokenCookie = new Cookie("accessToken", jwt);
//         tokenCookie.setPath("/");
//         tokenCookie.setHttpOnly(true);
//         tokenCookie.setSecure(true);
//         response.addCookie(tokenCookie);
    
//         Cookie idCookie = new Cookie("id", String.valueOf(TeacherEntity.getId()));
//         idCookie.setPath("/");
//         idCookie.setHttpOnly(true);
//         idCookie.setSecure(true);
//         response.addCookie(idCookie);
    
//         Cookie usernameCookie = new Cookie("username", TeacherEntity.getName());
//         usernameCookie.setPath("/");
//         usernameCookie.setHttpOnly(true);
//         usernameCookie.setSecure(true);
//         response.addCookie(usernameCookie);
    
//         // Create a JSON response with user information
//         response.getWriter().write(String.format("{\"email\": \"%s\", \"accessToken\": \"%s\", \"username\": \"%s\", \"id\": \"%d\"}", username, jwt, TeacherEntity.getName(), TeacherEntity.getId()));
//         response.getWriter().flush();
//     }
    
// }
