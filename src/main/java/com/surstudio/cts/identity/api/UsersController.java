package com.surstudio.cts.identity.api;

import com.surstudio.cts.identity.application.UsersService;
import com.surstudio.cts.identity.domain.AppUser;
import com.surstudio.cts.identity.dto.PatchUserRequest;
import com.surstudio.cts.identity.dto.UserMeResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UsersController {

    private final UsersService usersService;

    public UsersController(UsersService usersService) {
        this.usersService = usersService;
    }

    @GetMapping("/me")
    public UserMeResponse getMe(@AuthenticationPrincipal AppUser user) {
        return usersService.getMe(user);
    }

    @PatchMapping("/me")
    public UserMeResponse patchMe(@AuthenticationPrincipal AppUser user,
                                   @RequestBody PatchUserRequest request) {
        return usersService.patchMe(user, request);
    }
}
