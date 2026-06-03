package com.andrew.securetaskpro.security;

import com.andrew.securetaskpro.dto.AuthResponse;
import com.andrew.securetaskpro.dto.LoginRequest;
import com.andrew.securetaskpro.dto.RegisterRequest;
import com.andrew.securetaskpro.model.Organization;
import com.andrew.securetaskpro.model.User;
import com.andrew.securetaskpro.repository.OrganizationRepository;
import com.andrew.securetaskpro.repository.UserRepository;
import com.andrew.securetaskpro.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AuthServiceTest {

    private UserRepository userRepository;
    private OrganizationRepository organizationRepository;
    private JwtUtil jwtUtil;
    private PasswordEncoder passwordEncoder;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        organizationRepository = mock(OrganizationRepository.class);
        jwtUtil = mock(JwtUtil.class);
        passwordEncoder = mock(PasswordEncoder.class);
        authService = new AuthService(userRepository, passwordEncoder, jwtUtil, organizationRepository);
    }

    @Test
    void shouldSaveOrganizationAndUserAndReturnToken() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("andrew");
        request.setPassword("password");
        request.setOrganizationName("Acme");

        when(userRepository.findByUsername("andrew")).thenReturn(Optional.empty());
        when(organizationRepository.findByName("Acme")).thenReturn(Optional.empty());

        when(passwordEncoder.encode("password")).thenReturn("hashed");
        when(jwtUtil.generateToken("andrew")).thenReturn("jwt-token");

        AuthResponse response = authService.register(request);

        assertEquals("jwt-token", response.getToken());
        assertEquals("andrew", response.getUsername());
        assertEquals("ADMIN", response.getRole());

        verify(organizationRepository).save(any(Organization.class));

        //  user was saved with the hashed password and ADMIN role
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertEquals("andrew", savedUser.getUsername());
        assertEquals("hashed", savedUser.getPassword());
        assertEquals("ADMIN", savedUser.getRole());
    }

    @Test
    void shouldThrow409WhenUsernameAlreadyExists() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("andrew");
        request.setPassword("password");
        request.setOrganizationName("Acme");

        // a user with this username already exists
        User existing = new User("andrew", "hashed", "ADMIN", 1L);
        when(userRepository.findByUsername("andrew")).thenReturn(Optional.of(existing));

        // register() should reject with a 409 CONFLICT
        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> authService.register(request));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());

        verify(userRepository, never()).save(any(User.class));
        verify(organizationRepository, never()).save(any(Organization.class));
    }

    @Test
    void shouldThrow409WhenOrganizationNameAlreadyExists() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("andrew");
        request.setPassword("password");
        request.setOrganizationName("Acme");

        Organization existing = new Organization("Acme");
        when(organizationRepository.findByName("Acme")).thenReturn(Optional.of(existing));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> authService.register(request)
        );
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());

        verify(userRepository, never()).save(any(User.class));
        verify(organizationRepository, never()).save(any(Organization.class));
    }

    @Test
    void shouldLoginAndReturnToken() {
        LoginRequest request = new LoginRequest();
        request.setUsername("andrew");
        request.setPassword("password");

        // an existing user is found, with the stored (hashed) password
        User user = new User("andrew", "hashed", "ADMIN", 1L);
        when(userRepository.findByUsername("andrew")).thenReturn(Optional.of(user));

        // the raw password matches the stored hash
        when(passwordEncoder.matches("password", "hashed")).thenReturn(true);
        when(jwtUtil.generateToken("andrew")).thenReturn("jwt-token");

        AuthResponse response = authService.login(request);

        assertEquals("jwt-token", response.getToken());
        assertEquals("andrew", response.getUsername());
        assertEquals("ADMIN", response.getRole());

        // login is read-only: it must not write anything
        verify(userRepository, never()).save(any(User.class));
        verify(organizationRepository, never()).save(any(Organization.class));
    }

    @Test
    void shouldThrow401WhenUserNotFound() {
        LoginRequest request = new LoginRequest();
        request.setUsername("andrew");
        request.setPassword("password");

        // no user with this username exists
        when(userRepository.findByUsername("andrew")).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> authService.login(request));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());

        // it should never even check the password or issue a token
        verify(passwordEncoder, never()).matches(any(), any());
        verify(jwtUtil, never()).generateToken(any());
    }

    @Test
    void shouldThrow401WhenPasswordDoesNotMatch() {
        LoginRequest request = new LoginRequest();
        request.setUsername("andrew");
        request.setPassword("wrong-password");

        // the user exists, but the supplied password does not match the stored hash
        User user = new User("andrew", "hashed", "ADMIN", 1L);
        when(userRepository.findByUsername("andrew")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "hashed")).thenReturn(false);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> authService.login(request));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());

        verify(jwtUtil, never()).generateToken(any());
    }
}