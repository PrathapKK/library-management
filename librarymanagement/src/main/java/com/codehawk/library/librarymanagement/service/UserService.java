
//src/main/java/com/library/management/service/UserService.java
package com.codehawk.library.librarymanagement.service;

import com.codehawk.library.librarymanagement.exception.DuplicateResourceException;
import com.codehawk.library.librarymanagement.exception.ResourceNotFoundException;
import com.codehawk.library.librarymanagement.model.User;
import com.codehawk.library.librarymanagement.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
 
 private final UserRepository userRepository;
 private final PasswordEncoder passwordEncoder;
 
 @Autowired
 public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
     this.userRepository = userRepository;
     this.passwordEncoder = passwordEncoder;
 }
 
 public List<User> getAllUsers() {
     return userRepository.findAll();
 }
 
 public User getUserById(Long id) {
     return userRepository.findById(id)
             .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
 }
 
 public User getUserByEmail(String email) {
     return userRepository.findByEmail(email)
             .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
 }
 
 public User registerUser(User user) {
     if (userRepository.existsByEmail(user.getEmail())) {
         throw new DuplicateResourceException("Email already in use: " + user.getEmail());
     }
     
     // Encrypt password before saving
     user.setPassword(passwordEncoder.encode(user.getPassword()));
     return userRepository.save(user);
 }
 
 public User updateUser(Long id, User userDetails) {
     User user = getUserById(id);
     
     // If email is being changed, check if the new email is already in use
     if (!user.getEmail().equals(userDetails.getEmail()) && 
         userRepository.existsByEmail(userDetails.getEmail())) {
         throw new DuplicateResourceException("Email already in use: " + userDetails.getEmail());
     }
     
     user.setName(userDetails.getName());
     user.setEmail(userDetails.getEmail());
     user.setAddress(userDetails.getAddress());
     user.setPhone(userDetails.getPhone());
     
     // Only admin can change roles
     if (userDetails.getRole() != null) {
         user.setRole(userDetails.getRole());
     }
     
     return userRepository.save(user);
 }
 
 public void deleteUser(Long id) {
     User user = getUserById(id);
     if (!user.getTransactions().isEmpty()) {
         throw new IllegalStateException("Cannot delete user with active transactions");
     }
     userRepository.delete(user);
 }
 
 public User changePassword(Long id, String oldPassword, String newPassword) {
     User user = getUserById(id);
     
     if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
         throw new IllegalArgumentException("Current password is incorrect");
     }
     
     user.setPassword(passwordEncoder.encode(newPassword));
     return userRepository.save(user);
 }
}