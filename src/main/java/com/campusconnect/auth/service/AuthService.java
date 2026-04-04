package com.campusconnect.auth.service;

import com.campusconnect.auth.dto.AuthResponse;
import com.campusconnect.auth.dto.AuthUserResponse;
import com.campusconnect.auth.dto.LoginRequest;
import com.campusconnect.auth.dto.RegisterRequest;
import com.campusconnect.common.exception.BadRequestException;
import com.campusconnect.common.exception.ResourceNotFoundException;
import com.campusconnect.common.exception.UnauthorizedException;
import com.campusconnect.config.JwtService;
import com.campusconnect.enums.RoleName;
import com.campusconnect.user.dto.UserResponse;
import com.campusconnect.user.entity.Role;
import com.campusconnect.user.entity.User;
import com.campusconnect.user.repository.RoleRepository;
import com.campusconnect.user.repository.UserRepository;
import com.campusconnect.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;
  private final JwtService jwtService;
  private final UserService userService;

  @Transactional
  public UserResponse register(RegisterRequest req) {
    String email = req.getEmail().trim().toLowerCase();

    if (userRepository.existsByEmail(email)) {
      throw new BadRequestException("Email already exists");
    }

    Role studentRole = roleRepository.findByName(RoleName.ROLE_STUDENT)
        .orElseThrow(() -> new ResourceNotFoundException("Default role not configured: ROLE_STUDENT"));

    User user = User.builder()
        .fullName(req.getFullName().trim())
        .email(email)
        .password(passwordEncoder.encode(req.getPassword()))
        .course(req.getCourse())
        .yearOfStudy(req.getYearOfStudy())
        .reputationPoints(0)
        .isActive(true)
        .build();

    user.getRoles().add(studentRole);

    User saved;
    try {
      saved = userRepository.save(user);
    } catch (DataIntegrityViolationException ex) {
      // Handles race condition: concurrent requests can still hit the DB unique constraint.
      throw new BadRequestException("Email already exists");
    }
    log.info("User registered: {}", saved.getEmail());
    return userService.toResponse(saved);
  }

  @Transactional(readOnly = true)
  public AuthResponse login(LoginRequest req) {
    String email = req.getEmail().trim().toLowerCase();

    try {
      authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(email, req.getPassword())
      );
    } catch (BadCredentialsException ex) {
      throw new UnauthorizedException("Invalid email or password");
    } catch (DisabledException ex) {
      throw new UnauthorizedException("User is inactive");
    }

    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

    String token = jwtService.generateToken(user);

    log.info("User logged in: {}", user.getEmail());

    return AuthResponse.builder()
        .token(token)
        .user(AuthUserResponse.builder()
            .id(user.getId())
            .fullName(user.getFullName())
            .email(user.getEmail())
            .roles(user.getRoles().stream().map(r -> r.getName()).collect(java.util.stream.Collectors.toSet()))
            .build())
        .build();
  }
}
