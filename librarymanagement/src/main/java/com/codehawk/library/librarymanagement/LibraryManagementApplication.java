package com.codehawk.library.librarymanagement;



import java.util.Base64;

import javax.crypto.SecretKey;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@SpringBootApplication
@EnableScheduling
public class LibraryManagementApplication {
    public static void main(String[] args) {
    	//SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS512);
    	//System.out.println(Base64.getEncoder().encodeToString(key.getEncoded()));
    	
        SpringApplication.run(LibraryManagementApplication.class, args);
    }
}
//Adding comments