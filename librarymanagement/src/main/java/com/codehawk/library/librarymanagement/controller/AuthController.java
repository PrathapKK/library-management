
//---------- Authentication Controller ----------

//src/main/java/com/library/management/controller/AuthController.java
package com.codehawk.library.librarymanagement.controller;

import com.codehawk.library.librarymanagement.dto.JwtAuthResponse;
import com.codehawk.library.librarymanagement.dto.LoginDto;
import com.codehawk.library.librarymanagement.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

 private final AuthenticationManager authenticationManager;
 private final JwtTokenProvider tokenProvider;

 @Autowired
 public AuthController(AuthenticationManager authenticationManager, JwtTokenProvider tokenProvider) {
     this.authenticationManager = authenticationManager;
     this.tokenProvider = tokenProvider;
 }

 @PostMapping("/login")
 public ResponseEntity<JwtAuthResponse> authenticateUser(@RequestBody LoginDto loginDto) {
     Authentication authentication = authenticationManager.authenticate(
             new UsernamePasswordAuthenticationToken(
                     loginDto.getEmail(),
                     loginDto.getPassword()
             )
     );

     SecurityContextHolder.getContext().setAuthentication(authentication);

     // get token from token provider
     String token = tokenProvider.generateToken(authentication);

     return ResponseEntity.ok(new JwtAuthResponse(token));
 }
}
