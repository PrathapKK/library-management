
//src/main/java/com/library/management/security/JwtAuthenticationFilter.java
package com.codehawk.library.librarymanagement.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenProvider tokenProvider;
    
    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
    	
    	// Don't check for token on preflight (OPTIONS) requests
        if (request.getMethod().equals("OPTIONS")) {
            filterChain.doFilter(request, response);
            return;
        }
        try {
            // Get JWT token from request
            String token = getJwtFromRequest(request);
            
            System.out.println("Received Token from jwt from Request "+token);
            // Validate token and set authentication
            
            System.out.println("\n\n StringUtils.hasText(token) : = "+StringUtils.hasText(token));
            System.out.println("\n\n Validate Token : "+tokenProvider.validateToken(token));
            if (StringUtils.hasText(token) && tokenProvider.validateToken(token)) {
                String username = tokenProvider.getUsernameFromJWT(token);
                
                System.out.println("Username from Token "+username);
                UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);
                
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                        
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
                System.out.println("Authentication set in context: " + username);

            }else {
            	System.out.println("token doesnt have any text");
                System.out.println("Invalid or missing token");
            }
        } catch (Exception ex) {
            logger.error("Could not set user authentication in security context", ex);
        }
        
        filterChain.doFilter(request, response);
    }

    public String getJwtFromRequest(HttpServletRequest request) {
		System.out.println("Entered Method - getJwtFromRequest");

        String bearerToken = request.getHeader("Authorization");
        
        System.out.println("Authorization header: " + bearerToken);

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
        	System.out.println("Bearer Token from Request : "+bearerToken.substring(7));
    		System.out.println("Exiting Method - getJwtFromRequest returning token");
            return bearerToken;
        }
		System.out.println("Exiting Method - getJwtFromRequest returning null token");
        return null;
    }
}