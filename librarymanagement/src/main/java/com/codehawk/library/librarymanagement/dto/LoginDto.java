
//---------- Data Transfer Objects (DTOs) ----------

//src/main/java/com/library/management/dto/LoginDto.java
package com.codehawk.library.librarymanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginDto {
 private String email;
 private String password;
}