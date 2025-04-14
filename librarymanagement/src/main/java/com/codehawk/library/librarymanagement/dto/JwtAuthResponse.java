
//src/main/java/com/library/management/dto/JwtAuthResponse.java
package com.codehawk.library.librarymanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JwtAuthResponse {
 private String accessToken;
 private String tokenType = "Bearer";

 public JwtAuthResponse(String accessToken) {
     this.accessToken = accessToken;
 }
}