package com.andrew.securetaskpro.service;

import com.andrew.securetaskpro.dto.AuthResponse;
import com.andrew.securetaskpro.dto.LoginRequest;
import com.andrew.securetaskpro.dto.RegisterRequest;
import com.andrew.securetaskpro.model.Organization;
import com.andrew.securetaskpro.model.User;
import com.andrew.securetaskpro.repository.OrganizationRepository;
import com.andrew.securetaskpro.repository.UserRepository;
import com.andrew.securetaskpro.security.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil, OrganizationRepository organizationRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.organizationRepository = organizationRepository;
    }

    public AuthResponse register(RegisterRequest request) {

        if(userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists.");
        }
        if(organizationRepository.findByName(request.getOrganizationName()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Organization Name already exists");
        }

        Organization org = new Organization(request.getOrganizationName());
        organizationRepository.save(org);

        String hashed = passwordEncoder.encode(request.getPassword());
        User user = new User(request.getUsername(), hashed, "ADMIN", org.getId());
        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getUsername());
        return toResponse(user, token);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials."));

        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        String token = jwtUtil.generateToken(user.getUsername());
        return toResponse(user, token);

    }

    private AuthResponse toResponse(User user, String token) {
        return new AuthResponse(
                token,
                user.getUsername(),
                user.getRole(),
                user.getOrganizationId()
        );
    }

}
