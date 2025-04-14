package com.codehawk.library.librarymanagement.integration;

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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class UserIntegrationTest {

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

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        testUser = new User();
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setAddress("123 Test Street");
        testUser.setPhone("1234567890");
        testUser.setRole(User.Role.USER);

        testAdmin = new User();
        testAdmin.setName("Admin User");
        testAdmin.setEmail("admin@example.com");
        testAdmin.setPassword(passwordEncoder.encode("admin123"));
        testAdmin.setRole(User.Role.ADMIN);

        testUser = userRepository.save(testUser);
        testAdmin = userRepository.save(testAdmin);
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldGetAllUsers() throws Exception {
        // given: users are in DB from setUp

        // when
        ResultActions response = mockMvc.perform(get("/api/users")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        response.andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is(testUser.getName())))
                .andExpect(jsonPath("$[1].name", is(testAdmin.getName())))
                .andDo(print());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldGetUserById() throws Exception {
        // given: user is in DB from setUp

        // when
        ResultActions response = mockMvc.perform(get("/api/users/{id}", testUser.getId())
                .contentType(MediaType.APPLICATION_JSON));

        // then
        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testUser.getId()))
                .andExpect(jsonPath("$.name", is(testUser.getName())))
                .andExpect(jsonPath("$.email", is(testUser.getEmail())))
                .andDo(print());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldGetUserByEmail() throws Exception {
        // given: user is in DB from setUp

        // when
        ResultActions response = mockMvc.perform(get("/api/users/email/{email}", testUser.getEmail())
                .contentType(MediaType.APPLICATION_JSON));

        // then
        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testUser.getId()))
                .andExpect(jsonPath("$.email", is(testUser.getEmail())))
                .andDo(print());
    }

    @Test
    void shouldRegisterNewUser() throws Exception {
        // given
        User newUser = new User();
        newUser.setName("New User");
        newUser.setEmail("new@example.com");
        newUser.setPassword("newpassword");
        newUser.setAddress("456 New Street");
        newUser.setPhone("0987654321");

        // when
        ResultActions response = mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newUser)));

        // then
        response.andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is(newUser.getName())))
                .andExpect(jsonPath("$.email", is(newUser.getEmail())))
                .andDo(print());

        // Verify user is saved in DB
        Optional<User> savedUser = userRepository.findByEmail("new@example.com");
        assertThat(savedUser).isPresent();
        assertThat(savedUser.get().getName()).isEqualTo("New User");
        
        // Verify password is encoded
        assertThat(passwordEncoder.matches("newpassword", savedUser.get().getPassword())).isTrue();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldUpdateUser() throws Exception {
        // given
        User updatedUser = new User();
        updatedUser.setName("Updated Name");
        updatedUser.setEmail(testUser.getEmail()); // Keep same email
        updatedUser.setAddress("Updated Address");
        updatedUser.setPhone("9999999999");

        // when
        ResultActions response = mockMvc.perform(put("/api/users/{id}", testUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedUser)));

        // then
        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testUser.getId()))
                .andExpect(jsonPath("$.name", is(updatedUser.getName())))
                .andExpect(jsonPath("$.address", is(updatedUser.getAddress())))
                .andDo(print());

        // Verify user is updated in DB
        User updatedUserInDb = userRepository.findById(testUser.getId()).orElseThrow();
        assertThat(updatedUserInDb.getName()).isEqualTo(updatedUser.getName());
        assertThat(updatedUserInDb.getAddress()).isEqualTo(updatedUser.getAddress());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldDeleteUser() throws Exception {
        // given: user is in DB from setUp

        // when
        ResultActions response = mockMvc.perform(delete("/api/users/{id}", testUser.getId())
                .contentType(MediaType.APPLICATION_JSON));

        // then
        response.andExpect(status().isNoContent())
                .andDo(print());

        // Verify user is deleted from DB
        Optional<User> deletedUser = userRepository.findById(testUser.getId());
        assertThat(deletedUser).isEmpty();
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "USER")
    void shouldChangePassword() throws Exception {
        // given
        String oldPassword = "password123";
        String newPassword = "newPassword123";

        // when
        ResultActions response = mockMvc.perform(put("/api/users/{id}/change-password", testUser.getId())
                .param("oldPassword", oldPassword)
                .param("newPassword", newPassword)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        response.andExpect(status().isOk())
                .andDo(print());

        // Verify password is changed in DB
        User updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
        assertThat(passwordEncoder.matches(newPassword, updatedUser.getPassword())).isTrue();
        assertThat(passwordEncoder.matches(oldPassword, updatedUser.getPassword())).isFalse();
    }

    @Test
    void shouldReturnConflictWhenRegisteringWithExistingEmail() throws Exception {
        // given
        User duplicateUser = new User();
        duplicateUser.setName("Duplicate User");
        duplicateUser.setEmail(testUser.getEmail()); // Same email as existing user
        duplicateUser.setPassword("password");

        // when
        ResultActions response = mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicateUser)));

        // then
        response.andExpect(status().isConflict())
                .andDo(print());

        // Verify no additional user is saved
        List<User> users = userRepository.findAll();
        assertThat(users).hasSize(2); // Only the original 2 users
    }

    @Test
    void shouldReturnUnauthorizedForUnauthenticatedUser() throws Exception {
        // when - no authentication
        ResultActions response = mockMvc.perform(get("/api/users")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        response.andExpect(status().isUnauthorized())
                .andDo(print());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturnForbiddenForUnauthorizedRole() throws Exception {
        // when - USER role attempting ADMIN operation
        ResultActions response = mockMvc.perform(get("/api/users")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        response.andExpect(status().isForbidden())
                .andDo(print());
    }

    @Test
    @WithMockUser(username = "wronguser@example.com", roles = "USER")
    void shouldReturnForbiddenWhenChangingOtherUsersPassword() throws Exception {
        // given
        String oldPassword = "password123";
        String newPassword = "newPassword123";

        // when - different user attempting to change password
        ResultActions response = mockMvc.perform(put("/api/users/{id}/change-password", testUser.getId())
                .param("oldPassword", oldPassword)
                .param("newPassword", newPassword)
                .contentType(MediaType.APPLICATION_JSON));

        // then
        response.andExpect(status().isForbidden())
                .andDo(print());
    }
}