package com.exemplo.userservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(

        String name,

        @Email(message = "Invalid email format")
        String email,

        @Size(min = 6, message = "Password must have at least 6 characters")
        String password
) {}
