
//src/main/java/com/library/management/model/User.java
package com.codehawk.library.librarymanagement.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
 @Id
 @GeneratedValue(strategy = GenerationType.IDENTITY)
 private Long id;
 
 @Column(nullable = false)
 private String name;
 
 @Column(nullable = false, unique = true)
 private String email;
 
 @Column(nullable = false)
 private String password;
 
 private String address;
 
 private String phone;
 
 @Enumerated(EnumType.STRING)
 private Role role = Role.USER;
 
 @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
 private List<Transaction> transactions = new ArrayList<>();
 
 public enum Role {
     USER, LIBRARIAN, ADMIN
 }
}