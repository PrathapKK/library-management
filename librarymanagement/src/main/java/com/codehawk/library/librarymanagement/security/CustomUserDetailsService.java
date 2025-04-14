
//src/main/java/com/library/management/security/CustomUserDetailsService.java
package com.codehawk.library.librarymanagement.security;

import com.codehawk.library.librarymanagement.model.User;
import com.codehawk.library.librarymanagement.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

 private final UserRepository userRepository;

 @Autowired
 public CustomUserDetailsService(UserRepository userRepository) {
     this.userRepository = userRepository;
 }

 @Override
 public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
     User user = userRepository.findByEmail(email)
             .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

     return new org.springframework.security.core.userdetails.User(
             user.getEmail(),
             user.getPassword(),
             Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
     );
 }
}