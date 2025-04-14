package com.codehawk.library.librarymanagement.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JwtAuthenticationFilterTest {

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private UserDetails userDetails;

    @Mock
    private SecurityContext securityContext;

    private String token;
    private String username;

    @BeforeEach
    void setUp() {
        token = "valid_jwt_token";
        username = "test@example.com";
        
        // Reset the security context before each test
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldAuthenticateUserWhenValidTokenIsProvided() throws ServletException, IOException {
        // given
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(tokenProvider.validateToken(token)).thenReturn(true);
        when(tokenProvider.getUsernameFromJWT(token)).thenReturn(username);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
        
        // Mock the security context to verify authentication object is set
        when(securityContext.getAuthentication()).thenReturn(null);
        SecurityContextHolder.setContext(securityContext);

        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then
        verify(filterChain).doFilter(request, response);
        verify(tokenProvider).validateToken(token);
        verify(tokenProvider).getUsernameFromJWT(token);
        verify(userDetailsService).loadUserByUsername(username);
        verify(securityContext).setAuthentication(any(Authentication.class));
    }

    @Test
    void shouldContinueFilterChainWhenNoAuthHeader() throws ServletException, IOException {
        // given
        when(request.getHeader("Authorization")).thenReturn(null);

        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then
        verify(filterChain).doFilter(request, response);
        verify(tokenProvider, never()).validateToken(anyString());
        verify(userDetailsService, never()).loadUserByUsername(anyString());
    }

    @Test
    void shouldContinueFilterChainWhenInvalidAuthHeaderFormat() throws ServletException, IOException {
        // given
        when(request.getHeader("Authorization")).thenReturn("InvalidFormat");

        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then
        verify(filterChain).doFilter(request, response);
        verify(tokenProvider, never()).validateToken(anyString());
        verify(userDetailsService, never()).loadUserByUsername(anyString());
    }

    @Test
    void shouldContinueFilterChainWhenTokenIsInvalid() throws ServletException, IOException {
        // given
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(tokenProvider.validateToken(token)).thenReturn(false);

        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then
        verify(filterChain).doFilter(request, response);
        verify(tokenProvider).validateToken(token);
        verify(tokenProvider, never()).getUsernameFromJWT(anyString());
        verify(userDetailsService, never()).loadUserByUsername(anyString());
    }

    @Test
    void shouldHandleExceptionAndContinueFilterChain() throws ServletException, IOException {
        // given
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(tokenProvider.validateToken(token)).thenReturn(true);
        when(tokenProvider.getUsernameFromJWT(token)).thenReturn(username);
        when(userDetailsService.loadUserByUsername(username)).thenThrow(new RuntimeException("Test exception"));
        
        // when
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // then
        verify(filterChain).doFilter(request, response);
        // Even with an exception, the filter chain should continue
    }

    @Test
    void shouldExtractTokenCorrectly() throws ServletException, IOException {
        // given
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        
        // Create a spy of the filter to test protected method
        JwtAuthenticationFilter filterSpy = spy(jwtAuthenticationFilter);
        
        // when
        String extractedToken = filterSpy.getJwtFromRequest(request);
        
        // then
        assertThat(extractedToken).isEqualTo(token);
    }

    @Test
    void shouldReturnNullWhenTokenIsEmpty() throws ServletException, IOException {
        // given
        when(request.getHeader("Authorization")).thenReturn("Bearer ");
        
        // Create a spy of the filter to test protected method
        JwtAuthenticationFilter filterSpy = spy(jwtAuthenticationFilter);
        
        // when
        String extractedToken = filterSpy.getJwtFromRequest(request);
        
        // then
        assertThat(extractedToken).isNull();
    }

    @Test
    void shouldReturnNullWhenNoAuthorizationHeader() throws ServletException, IOException {
        // given
        when(request.getHeader("Authorization")).thenReturn(null);
        
        // Create a spy of the filter to test protected method
        JwtAuthenticationFilter filterSpy = spy(jwtAuthenticationFilter);
        
        // when
        String extractedToken = filterSpy.getJwtFromRequest(request);
        
        // then
        assertThat(extractedToken).isNull();
    }
}