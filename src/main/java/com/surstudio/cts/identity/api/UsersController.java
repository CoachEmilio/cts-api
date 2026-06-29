package com.surstudio.cts.identity.api;

import com.surstudio.cts.identity.domain.AppUser;
import com.surstudio.cts.identity.domain.UserRepository;
import com.surstudio.cts.identity.dto.PatchUserRequest;
import com.surstudio.cts.identity.dto.UserMeResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UsersController {

    private final UserRepository userRepository;

    public UsersController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/me")
    @Transactional(readOnly = true)
    public UserMeResponse getMe(@AuthenticationPrincipal AppUser user) {
        return toDto(user);
    }

    @PatchMapping("/me")
    @Transactional
    public UserMeResponse patchMe(@AuthenticationPrincipal AppUser user,
                                   @RequestBody PatchUserRequest request) {
        if (request.onboardingComplete() != null) {
            user.setOnboardingComplete(request.onboardingComplete());
        }
        if (request.avatarUrl() != null) {
            user.setAvatarUrl(request.avatarUrl());
        }
        if (request.fullName() != null) {
            user.setFullName(request.fullName());
        }
        if (request.phone() != null) {
            user.setPhone(request.phone());
        }
        userRepository.save(user);
        return toDto(user);
    }

    private UserMeResponse toDto(AppUser user) {
        return new UserMeResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getPhone(),
                user.getRole().name().toLowerCase(),
                user.isOnboardingComplete(),
                user.getAvatarUrl()
        );
    }
}