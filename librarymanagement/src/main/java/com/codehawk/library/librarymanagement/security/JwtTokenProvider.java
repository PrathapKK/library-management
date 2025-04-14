// src/main/java/com/library/management/security/JwtTokenProvider.java
package com.codehawk.library.librarymanagement.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

import java.security.Key;
import java.util.Arrays;
import java.util.Date;

@Component
public class JwtTokenProvider {

	@Value("${app.jwt-secret}")
	private String jwtSecret;

	@Value("${app.jwt-expiration-milliseconds}")
	private int jwtExpirationInMs;

	private Key key;

	@PostConstruct
	public void init() {
		System.out.println("from init api JWT secret: " + jwtSecret);
		byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
		this.key = Keys.  hmacShaKeyFor(keyBytes);
		System.out.println("Decoded keybytes : " + Arrays.toString(keyBytes));
	}

	public String generateToken(Authentication authentication) {
		String username = authentication.getName();
		Date currentDate = new Date();
		Date expireDate = new Date(currentDate.getTime() + jwtExpirationInMs);
		String generatedToken=Jwts.builder().setSubject(username).setIssuedAt(new Date()).setExpiration(expireDate)
				.signWith(this.key, SignatureAlgorithm.HS512).compact();
		System.out.println("Method - generateToken - generatedToken is :  "+ generatedToken);
		return generatedToken;
		// raw secret string works here
	}

	public String getUsernameFromJWT(String token) { // Update this method too
		System.out.println("Entered Method - getUsernameFromJWT");
		System.out.println("From getUsernameFromJWT -Authorization header: " + token);
		//token=token.substring(7);
		System.out.println("From getUsernameFromJWT -Key length: " + key.getEncoded().length); // Should be >= 64
		
		if (token.startsWith("Bearer ")) {
		    token = token.substring(7).trim();
		}

		Claims claims = Jwts.parserBuilder().setSigningKey(this.key).build().parseClaimsJws(token).getBody();
		String username=claims.getSubject();
		System.out.println("Username parsed from JWT Token : "+username);
		System.out.println("Exited Method - getUsernameFromJWT");
		return claims.getSubject();
	}

	public boolean validateToken(String token) {
		System.out.println("Entered Method - validateToken");

		try {
			
			if (token == null || !token.startsWith("Bearer ")) {
	            System.out.println("Token is missing or does not start with Bearer");
	            return false;
	        }
			// Remove 'Bearer ' prefix
	        token = token.substring(7).trim();

	        // Log the token if needed
	        System.out.println("Validating token: " + token);
	        
			// Jwts.parser().setSigningKey(key).parseClaimsJws(token);
			System.out.println("From ValidateToken - Key length (bytes): " + this.key.getEncoded().length);
			System.out.println("From ValidateToken - Authorization header: " + token);
			//token=token.substring(7);
			//token=token.trim();
			System.out.println("Method ValidateToken- Trimmed(substring(7) token : "+token);
			System.out.println("Method ValidateToken - this.key.getAlgorithm()"+this.key.getAlgorithm()+" this.key.getEncoded()"
					+this.key.getEncoded().toString());
			Jwts.parserBuilder().setSigningKey(this.key).build().parseClaimsJws(token);
			System.out.println("Exited Method - validateToken returning true");

			return true;
		} catch (SignatureException ex) {
			System.out.println("Invalid JWT signature: " + ex.getMessage());
			System.out.println("Exited Method - validateToken  returning  false");
			return false;
		} catch (MalformedJwtException ex) {
			System.out.println("Invalid JWT token: " + ex.getMessage());
			System.out.println("Exited Method - validateToken  returning  false");
			return false;
		} catch (ExpiredJwtException ex) {
			System.out.println("Expired JWT token: " + ex.getMessage());
			System.out.println("Exited Method - validateToken  returning  false");
			return false;
		} catch (UnsupportedJwtException ex) {
			System.out.println("Unsupported JWT token: " + ex.getMessage());
			System.out.println("Exited Method - validateToken  returning  false");
			return false;
		} catch (IllegalArgumentException ex) {
			System.out.println("JWT claims string is empty: " + ex.getMessage());
			System.out.println("Exited Method - validateToken  returning  false");
			return false;
		} catch (Exception ex) {
			System.out.println("Unexpected error: " + ex.getMessage());
			System.out.println("Exited Method - validateToken  returning  false");
			return false;
		}
	}

	// Hardcoded secret key to eliminate errors

	// Use a hardcoded secret for now to eliminate config issues
	/*
	 * private static final String SECRET_KEY =
	 * "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970"; private
	 * static final long EXPIRATION_TIME = 86400000; // 24 hours
	 * 
	 * private final Key key;
	 * 
	 * public JwtTokenProvider() { byte[] keyBytes =
	 * Decoders.BASE64.decode(SECRET_KEY); this.key = Keys.hmacShaKeyFor(keyBytes);
	 * }
	 * 
	 * public String generateToken(Authentication authentication) { String username
	 * = authentication.getName(); Date currentDate = new Date(); Date expireDate =
	 * new Date(currentDate.getTime() + EXPIRATION_TIME);
	 * 
	 * return Jwts.builder() .setSubject(username) .setIssuedAt(currentDate)
	 * .setExpiration(expireDate) .signWith(key) .compact(); }
	 * 
	 * public String getUsernameFromJWT(String token) { Claims claims =
	 * Jwts.parserBuilder() .setSigningKey(key) .build() .parseClaimsJws(token)
	 * .getBody(); return claims.getSubject(); }
	 * 
	 * public boolean validateToken(String token) { try { Jwts.parserBuilder()
	 * .setSigningKey(key) .build() .parseClaimsJws(token); return true; } catch
	 * (Exception ex) { System.out.println("Token validation error: " +
	 * ex.getMessage()); return false; } }
	 */
}