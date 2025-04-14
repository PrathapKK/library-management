
//src/main/java/com/library/management/dto/UserDto.java
package com.codehawk.library.librarymanagement.dto;

import com.codehawk.library.librarymanagement.model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
 private Long id;
 
 @NotEmpty(message = "Name cannot be empty")
 private String name;
 
 @NotEmpty(message = "Email cannot be empty")
 @Email(message = "Email must be valid")
 private String email;
 
 private String address;
 
 private String phone;
 
 private User.Role role;
}