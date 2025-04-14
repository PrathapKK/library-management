package com.codehawk.library.librarymanagement.integration;

import com.codehawk.library.librarymanagement.dto.JwtAuthResponse;
import com.codehawk.library.librarymanagement.dto.LoginDto;
import com.codehawk.library.librarymanagement.model.User;
import com.codehawk.library.librarymanagement.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AuthenticationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private User testAdmin;
    private User testLibrarian;
    private final String rawPassword = "password123";

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        // Create test user
        testUser = new User();
        testUser.setName("Test User");
        testUser.setEmail("user@example.com");
        testUser.setPassword(passwordEncoder.encode(rawPassword));
        testUser.setRole(User.Role.USER);
        testUser = userRepository.save(testUser);

        // Create test admin
        testAdmin = new User();
        testAdmin.setName("Test Admin");
        testAdmin.setEmail("admin@example.com");
        testAdmin.setPassword(passwordEncoder.encode(rawPassword));
        testAdmin.setRole(User.Role.ADMIN);
        testAdmin = userRepository.save(testAdmin);

        // Create test librarian
        testLibrarian = new User();
        testLibrarian.setName("Test Librarian");
        testLibrarian.setEmail("librarian@example.com");
        testLibrarian.setPassword(passwordEncoder.encode(rawPassword));
        testLibrarian.setRole(User.Role.LIBRARIAN);
        testLibrarian = userRepository.save(testLibrarian);
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    void shouldAuthenticateUserAndReturnJwtToken() throws Exception {
        // given
        LoginDto loginDto = new LoginDto();
        loginDto.setEmail(testUser.getEmail());
        loginDto.setPassword(rawPassword);

        // when
        ResultActions response = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDto)));

        // then
        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", notNullValue()))
                .andExpect(jsonPath("$.tokenType", is("Bearer")))
                .andDo(print());

        // Extract token for further validation
        MvcResult result = response.andReturn();
        String contentAsString = result.getResponse().getContentAsString();
        JwtAuthResponse authResponse = objectMapper.readValue(contentAsString, JwtAuthResponse.class);
        
        assertThat(authResponse.getAccessToken()).isNotNull();
        assertThat(authResponse.getTokenType()).isEqualTo("Bearer");
    }

    @Test
    void shouldAuthenticateAdminAndReturnJwtToken() throws Exception {
        // given
        LoginDto loginDto = new LoginDto();
        loginDto.setEmail(testAdmin.getEmail());
        loginDto.setPassword(rawPassword);

        // when
        ResultActions response = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDto)));

        // then
        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", notNullValue()))
                .andDo(print());
    }

    @Test
    void shouldAuthenticateLibrarianAndReturnJwtToken() throws Exception {
        // given
        LoginDto loginDto = new LoginDto();
        loginDto.setEmail(testLibrarian.getEmail());
        loginDto.setPassword(rawPassword);

        // when
        ResultActions response = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDto)));

        // then
        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", notNullValue()))
                .andDo(print());
    }

    @Test
    void shouldReturnUnauthorizedForInvalidCredentials() throws Exception {
        // given
        LoginDto loginDto = new LoginDto();
        loginDto.setEmail(testUser.getEmail());
        loginDto.setPassword("wrongpassword");

        // when
        ResultActions response = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDto)));

        // then
        response.andExpect(status().isUnauthorized())
                .andDo(print());
    }

    @Test
    void shouldReturnBadRequestForMalformedRequest() throws Exception {
        // when
        ResultActions response = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{malformedJson}"));

        // then
        response.andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    void shouldReturnBadRequestForMissingCredentials() throws Exception {
        // given
        LoginDto emptyLoginDto = new LoginDto();

        // when
        ResultActions response = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emptyLoginDto)));

        // then
        response.andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    void shouldReturnUnauthorizedForNonExistentUser() throws Exception {
        // given
        LoginDto loginDto = new LoginDto();
        loginDto.setEmail("nonexistent@example.com");
        loginDto.setPassword(rawPassword);

        // when
        ResultActions response = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDto)));

        // then
        response.andExpect(status().isUnauthorized())
                .andDo(print());
    }

    @Test
    void shouldAccessProtectedEndpointWithValidToken() throws Exception {
        // given
        // First, get a valid token
        LoginDto loginDto = new LoginDto();
        loginDto.setEmail(testAdmin.getEmail());
        loginDto.setPassword(rawPassword);

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDto)))
                .andReturn();

        String contentAsString = loginResult.getResponse().getContentAsString();
        JwtAuthResponse authResponse = objectMapper.readValue(contentAsString, JwtAuthResponse.class);
        String token = authResponse.getAccessToken();

        // when
        ResultActions response = mockMvc.perform(get("/api/users")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        response.andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    void shouldRejectAccessToProtectedEndpointWithInvalidToken() throws Exception {
        // when
        ResultActions response = mockMvc.perform(get("/api/users")
                .header("Authorization", "Bearer invalidtoken")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        response.andExpect(status().isUnauthorized())
                .andDo(print());
    }

    @Test
    void shouldRejectAccessToProtectedEndpointWithoutToken() throws Exception {
        // when
        ResultActions response = mockMvc.perform(get("/api/users")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        response.andExpect(status().isUnauthorized())
                .andDo(print());
    }

    @Test
    void shouldRespectRoleBasedAccessControl() throws Exception {
        // given
        // Get a token for a regular user
        LoginDto loginDto = new LoginDto();
        loginDto.setEmail(testUser.getEmail());
        loginDto.setPassword(rawPassword);

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDto)))
                .andReturn();

        String contentAsString = loginResult.getResponse().getContentAsString();
        JwtAuthResponse authResponse = objectMapper.readValue(contentAsString, JwtAuthResponse.class);
        String token = authResponse.getAccessToken();

        // when - try to access admin endpoint with user token
        ResultActions response = mockMvc.perform(get("/api/users")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        response.andExpect(status().isForbidden())
                .andDo(print());
    }
}