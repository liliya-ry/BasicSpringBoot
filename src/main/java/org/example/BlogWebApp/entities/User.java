package org.example.BlogWebApp.entities;

import java.util.Random;

public class User {
    private static final Random RANDOM = new Random();

    public int id;
    public String username;
    public String password;
    public String email;
    public String firstName;
    public String lastName;
    public int salt;
    public String role;

    public User(String username,
                String password,
                String email,
                String firstName,
                String lastName,
                String role) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
    }

    public int generateSalt() {
        return this.salt = RANDOM.nextInt();
    }
}