package com.exemplo.userservice.service;

import com.exemplo.userservice.dto.request.UpdateUserRequest;
import com.exemplo.userservice.dto.response.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    UserResponse findById(Long id);

    Page<UserResponse> findAll(Pageable pageable);

    UserResponse update(Long id, UpdateUserRequest request);

    void delete(Long id);
}
