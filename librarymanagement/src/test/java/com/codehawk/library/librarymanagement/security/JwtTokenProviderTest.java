package com.codehawk.library.librarymanagement.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class JwtTokenProviderTest {

    @InjectMocks
    private JwtTokenProvider tokenProvider;

    @Mock
    private Authentication authentication;

    private String jwtSecret = "testJwtSecretKeyWhichIsLongEnoughForHmacSHA512Algorithm";
    private int jwtExpirationInMs = 3600000; // 1 hour
    private String username = "test@example.com";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(tokenProvider, "jwtSecret", jwtSecret);
        ReflectionTestUtils.setField(tokenProvider, "jwtExpirationInMs", jwtExpirationInMs);
    }

    @Test
    void shouldGenerateTokenFromAuthentication() {
        // given
        when(authentication.getName()).thenReturn(username);

        // when
        String token = tokenProvider.generateToken(authentication);

        // then
        assertThat(token).isNotEmpty();
        
        // Parse the token to verify its contents
        Claims claims = Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody();
                
        assertThat(claims.getSubject()).isEqualTo(username);
    }

    @Test
    void shouldExtractUsernameFromToken() {
        // given
        String token = generateSampleToken();

        // when
        String extractedUsername = tokenProvider.getUsernameFromJWT(token);

        // then
        assertThat(extractedUsername).isEqualTo(username);
    }

    @Test
    void shouldValidateToken() {
        // given
        String token = generateSampleToken();

        // when
        boolean isValid = tokenProvider.validateToken(token);

        // then
        assertThat(isValid).isTrue();
    }

    @Test
    void shouldNotValidateTokenWithInvalidSignature() {
        // given
        String token = Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationInMs))
                .signWith(SignatureAlgorithm.HS512, "differentSecretKey")
                .compact();

        // when
        boolean isValid = tokenProvider.validateToken(token);

        // then
        assertThat(isValid).isFalse();
    }

    @Test
    void shouldNotValidateExpiredToken() {
        // given
        String token = Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() - 1000)) // Expired token
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();

        // when
        boolean isValid = tokenProvider.validateToken(token);

        // then
        assertThat(isValid).isFalse();
    }

    @Test
    void shouldNotValidateMalformedToken() {
        // given
        String token = "malformedToken";

        // when
        boolean isValid = tokenProvider.validateToken(token);

        // then
        assertThat(isValid).isFalse();
    }

    @Test
    void shouldNotValidateEmptyToken() {
        // given
        String token = "";

        // when
        boolean isValid = tokenProvider.validateToken(token);

        // then
        assertThat(isValid).isFalse();
    }

    private String generateSampleToken() {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationInMs))
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }
}