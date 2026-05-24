package com.exemplo.userservice.mapper;

import com.exemplo.userservice.domain.User;
import com.exemplo.userservice.dto.response.UserResponse;

public class UserMapper {

    private UserMapper() {}

    public static UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
