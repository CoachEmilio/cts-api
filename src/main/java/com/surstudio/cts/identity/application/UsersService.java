package com.surstudio.cts.identity.application;

import com.surstudio.cts.identity.domain.AppUser;
import com.surstudio.cts.identity.domain.UserRepository;
import com.surstudio.cts.identity.dto.PatchUserRequest;
import com.surstudio.cts.identity.dto.UserMeResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UsersService {

    private final UserRepository userRepository;

    public UsersService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public UserMeResponse getMe(AppUser user) {
        return toDto(user);
    }

    public UserMeResponse patchMe(AppUser user, PatchUserRequest request) {
        if (request.onboardingComplete() != null) user.setOnboardingComplete(request.onboardingComplete());
        if (request.avatarUrl() != null)          user.setAvatarUrl(request.avatarUrl());
        if (request.fullName() != null)           user.setFullName(request.fullName());
        if (request.phone() != null)              user.setPhone(request.phone());
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
