package com.sam.sammart.dto;

public class UserResponseDto {
    private long id;
    private String name;
    private String email;
    private String role;

    public UserResponseDto(long id, String name, String email, String role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
    }

    public long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
}
