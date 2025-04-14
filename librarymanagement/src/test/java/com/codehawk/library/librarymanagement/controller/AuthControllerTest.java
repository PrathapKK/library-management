package com.codehawk.library.librarymanagement.controller;

import com.codehawk.library.librarymanagement.dto.JwtAuthResponse;
import com.codehawk.library.librarymanagement.dto.LoginDto;
import com.codehawk.library.librarymanagement.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private JwtTokenProvider tokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    private LoginDto loginDto;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        // Setup test login credentials
        loginDto = new LoginDto();
        loginDto.setEmail("test@example.com");
        loginDto.setPassword("password123");

        // Mock authentication object
        authentication = new UsernamePasswordAuthenticationToken(
                loginDto.getEmail(), loginDto.getPassword());
    }

    @Test
    void shouldAuthenticateUserAndReturnJwt() throws Exception {
        // Set up the mocked behavior
        given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .willReturn(authentication);
        given(tokenProvider.generateToken(authentication)).willReturn("mocked_jwt_token");

        // Perform the request and validate the response
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", is("mocked_jwt_token")))
                .andExpect(jsonPath("$.tokenType", is("Bearer")));
    }

    @Test
    void shouldReturnUnauthorizedForInvalidCredentials() throws Exception {
        // Set up the mocked behavior to simulate authentication failure
        given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .willThrow(new org.springframework.security.authentication.BadCredentialsException("Bad credentials"));

        // Perform the request and validate the response
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldHandleEmptyCredentials() throws Exception {
        // Empty credentials
        LoginDto emptyLoginDto = new LoginDto();
        
        // Perform the request and validate the response
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emptyLoginDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldHandleMalformedJson() throws Exception {
        // Perform the request with malformed JSON and validate the response
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{malformed json}"))
                .andExpect(status().isBadRequest());
    }
}