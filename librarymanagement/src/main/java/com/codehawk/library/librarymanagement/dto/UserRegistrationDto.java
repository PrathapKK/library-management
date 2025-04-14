

//src/main/java/com/library/management/dto/UserRegistrationDto.java
package com.codehawk.library.librarymanagement.dto;
   
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRegistrationDto {
 @NotEmpty(message = "Name cannot be empty")
 private String name;
 
 @NotEmpty(message = "Email cannot be empty")
 @Email(message = "Email must be valid")
 private String email;
 
 @NotEmpty(message = "Password cannot be empty")
 @Size(min = 6, message = "Password must be at least 6 characters")
 private String password;
 
 private String address;
 
 private String phone;
}