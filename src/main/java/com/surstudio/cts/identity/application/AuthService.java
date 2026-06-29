package com.surstudio.cts.identity.application;

import com.surstudio.cts.common.ConflictException;
import com.surstudio.cts.common.security.JwtService;
import com.surstudio.cts.identity.domain.AppUser;
import com.surstudio.cts.identity.domain.UserRepository;
import com.surstudio.cts.identity.dto.AuthResponse;
import com.surstudio.cts.identity.dto.LoginRequest;
import com.surstudio.cts.identity.dto.RegisterRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("Email already registered: " + request.email());
        }
        var user = new AppUser();
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setFullName(request.fullName());
        user.setPhone(request.phone());
        userRepository.save(user);
        return toAuthResponse(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );
        var user = userRepository.findByEmail(request.email()).orElseThrow();
        return toAuthResponse(user);
    }

    private AuthResponse toAuthResponse(AppUser user) {
        return new AuthResponse(
                jwtService.generateToken(user),
                user.getRole().name().toLowerCase(),
                user.isOnboardingComplete()
        );
    }
}