package com.codehawk.library.librarymanagement.security;

import com.codehawk.library.librarymanagement.model.User;
import com.codehawk.library.librarymanagement.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    private User testUser;
    private String testEmail;
    private String testPassword;

    @BeforeEach
    void setUp() {
        testEmail = "test@example.com";
        testPassword = "password123";
        
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Test User");
        testUser.setEmail(testEmail);
        testUser.setPassword(testPassword);
        testUser.setRole(User.Role.USER);
    }

    @Test
    void shouldLoadUserByUsername() {
        // given
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));

        // when
        UserDetails userDetails = userDetailsService.loadUserByUsername(testEmail);

        // then
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo(testEmail);
        assertThat(userDetails.getPassword()).isEqualTo(testPassword);
        
        // Verify the granted authorities
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        assertThat(authorities).isNotEmpty();
        assertThat(authorities).hasSize(1);
        assertThat(authorities.iterator().next()).isEqualTo(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Test
    void shouldLoadUserByUsernameWithLibrarianRole() {
        // given
        testUser.setRole(User.Role.LIBRARIAN);
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));

        // when
        UserDetails userDetails = userDetailsService.loadUserByUsername(testEmail);

        // then
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        assertThat(authorities).isNotEmpty();
        assertThat(authorities).hasSize(1);
        assertThat(authorities.iterator().next()).isEqualTo(new SimpleGrantedAuthority("ROLE_LIBRARIAN"));
    }

    @Test
    void shouldLoadUserByUsernameWithAdminRole() {
        // given
        testUser.setRole(User.Role.ADMIN);
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));

        // when
        UserDetails userDetails = userDetailsService.loadUserByUsername(testEmail);

        // then
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        assertThat(authorities).isNotEmpty();
        assertThat(authorities).hasSize(1);
        assertThat(authorities.iterator().next()).isEqualTo(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        // given
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // when & then
        assertThrows(UsernameNotFoundException.class, () -> {
            userDetailsService.loadUserByUsername("nonexisting@example.com");
        });
    }

    @Test
    void shouldCreateCorrectUserDetailsObject() {
        // given
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));

        // when
        UserDetails userDetails = userDetailsService.loadUserByUsername(testEmail);

        // then
        assertThat(userDetails).isNotNull();
        
        // Verify standard userdetails properties
        assertThat(userDetails.isAccountNonExpired()).isTrue();
        assertThat(userDetails.isAccountNonLocked()).isTrue();
        assertThat(userDetails.isCredentialsNonExpired()).isTrue();
        assertThat(userDetails.isEnabled()).isTrue();
    }
}