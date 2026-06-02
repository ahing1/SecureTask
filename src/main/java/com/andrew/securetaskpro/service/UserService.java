package com.andrew.securetaskpro.service;

import com.andrew.securetaskpro.dto.InviteRequest;
import com.andrew.securetaskpro.dto.UserResponse;
import com.andrew.securetaskpro.model.User;
import com.andrew.securetaskpro.repository.UserRepository;
import com.andrew.securetaskpro.security.SecurityHelper;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityHelper securityHelper;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, SecurityHelper securityHelper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.securityHelper = securityHelper;
    }

    public UserResponse inviteUser(InviteRequest request) {
        if(userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        }

        String hashed = passwordEncoder.encode(request.getPassword());
        User user = new User(
                request.getUsername(),
                hashed,
                request.getRole(),
                securityHelper.getCurrentOrganizationId()
        );

        User saved = userRepository.save(user);

        return toResponse(saved);
    }

    public List<UserResponse> getUsersInOrganization() {
        Long orgId = securityHelper.getCurrentOrganizationId();

        return userRepository.findByOrganizationId(orgId).stream()
                .map(this::toResponse)
                .toList();
    }

    public void removeUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if(!user.getOrganizationId().equals(securityHelper.getCurrentOrganizationId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User does not belong is current organization");
        }

        userRepository.deleteById(id);
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getRole(),
                user.getOrganizationId()
        );
    }

}
