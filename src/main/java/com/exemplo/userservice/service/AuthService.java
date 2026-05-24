package com.exemplo.userservice.service;

import com.exemplo.userservice.domain.User;
import com.exemplo.userservice.dto.request.CreateUserRequest;
import com.exemplo.userservice.dto.request.LoginRequest;
import com.exemplo.userservice.dto.response.TokenResponse;
import com.exemplo.userservice.dto.response.UserResponse;
import com.exemplo.userservice.exception.EmailAlreadyExistsException;
import com.exemplo.userservice.mapper.UserMapper;
import com.exemplo.userservice.repository.UserRepository;
import com.exemplo.userservice.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public UserResponse register(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException(request.email());
        }

        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .build();

        return UserMapper.toResponse(userRepository.save(user));
    }

    public TokenResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        String token = jwtService.generateToken(request.email());
        return new TokenResponse(token, jwtService.getExpirationInstant());
    }
}
