package com.codegen.dto;

import lombok.Data;

@Data
public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private String role;
    private String fullName;
    private String company;
    private String jobTitle;
    private String experienceYears;
}