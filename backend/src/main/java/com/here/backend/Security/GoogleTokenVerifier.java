// package com.shouq.project.Security;
// import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
// import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
// import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
// import com.google.api.client.json.jackson2.JacksonFactory;

// import java.util.Collections;

// public class GoogleTokenVerifier {

//     private final GoogleIdTokenVerifier verifier;

//     public GoogleTokenVerifier() throws Exception {
//         verifier = new GoogleIdTokenVerifier.Builder(GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory.getDefaultInstance())
//                 .setAudience(Collections.singletonList("44788364330-kadh6flrqpcik0ou46s1iuuf86hncv1u.apps.googleusercontent.com"))
//                 .build();
//     }

//     public GoogleIdToken verify(String idTokenString) throws Exception {
//         return verifier.verify(idTokenString);
//     }
// }
