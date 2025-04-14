
//src/main/java/com/library/management/controller/UserController.java

package com.codehawk.library.librarymanagement.controller;


import com.codehawk.library.librarymanagement.model.User;
import com.codehawk.library.librarymanagement.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
 
 private final UserService userService;
 
 @Autowired
 public UserController(UserService userService) {
     this.userService = userService;
 }
 
 @GetMapping
 public ResponseEntity<List<User>> getAllUsers() {
     return ResponseEntity.ok(userService.getAllUsers());
 }
 
 @GetMapping("/{id}")
 public ResponseEntity<User> getUserById(@PathVariable Long id) {
     return ResponseEntity.ok(userService.getUserById(id));
 }
 
 @GetMapping("/email/{email}")
 public ResponseEntity<User> getUserByEmail(@PathVariable String email) {
     return ResponseEntity.ok(userService.getUserByEmail(email));
 }
 
 @PostMapping("/register")
 public ResponseEntity<User> registerUser(@Valid @RequestBody User user) {
     return new ResponseEntity<>(userService.registerUser(user), HttpStatus.CREATED);
 }
 
 @PutMapping("/{id}")
 public ResponseEntity<User> updateUser(@PathVariable Long id, @Valid @RequestBody User user) {
     return ResponseEntity.ok(userService.updateUser(id, user));
 }
 
 @DeleteMapping("/{id}")
 public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
     userService.deleteUser(id);
     return ResponseEntity.noContent().build();
 }
 
 @PutMapping("/{id}/change-password")
 public ResponseEntity<User> changePassword(
         @PathVariable Long id,
         @RequestParam String oldPassword,
         @RequestParam String newPassword) {
     return ResponseEntity.ok(userService.changePassword(id, oldPassword, newPassword));
 }
}