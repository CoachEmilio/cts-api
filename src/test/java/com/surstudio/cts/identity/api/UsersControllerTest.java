package com.surstudio.cts.identity.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.surstudio.cts.common.security.JwtService;
import com.surstudio.cts.common.security.SecurityConfig;
import com.surstudio.cts.common.security.UserDetailsConfig;
import com.surstudio.cts.identity.application.UsersService;
import com.surstudio.cts.identity.domain.AppUser;
import com.surstudio.cts.identity.domain.Role;
import com.surstudio.cts.identity.domain.UserRepository;
import com.surstudio.cts.identity.dto.UserMeResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UsersController.class)
@Import({SecurityConfig.class, UserDetailsConfig.class})
class UsersControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean JwtService jwtService;
    @MockBean UserRepository userRepository;
    @MockBean UsersService usersService;

    private AppUser stubUser;

    @BeforeEach
    void setUp() {
        stubUser = new AppUser();
        stubUser.setEmail("coach@test.com");
        stubUser.setPassword("hashed");
        stubUser.setRole(Role.CANDIDATE);
        stubUser.setFullName("María García");
        when(userRepository.findByEmail("coach@test.com")).thenReturn(Optional.of(stubUser));

        var meResponse = new UserMeResponse(1L, "coach@test.com", "María García", null, "candidate", false, null);
        when(usersService.getMe(any())).thenReturn(meResponse);
        when(usersService.patchMe(any(), any())).thenReturn(meResponse);
    }

    @Test
    @WithUserDetails(value = "coach@test.com", setupBefore = org.springframework.security.test.context.support.TestExecutionEvent.TEST_EXECUTION)
    void getMe_returnsUserData() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("coach@test.com"))
                .andExpect(jsonPath("$.role").value("candidate"))
                .andExpect(jsonPath("$.onboardingComplete").value(false));
    }

    @Test
    @WithUserDetails(value = "coach@test.com", setupBefore = org.springframework.security.test.context.support.TestExecutionEvent.TEST_EXECUTION)
    void patchMe_updatesOnboardingComplete() throws Exception {
        mockMvc.perform(patch("/api/v1/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"onboardingComplete": true}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("coach@test.com"));
    }

    @Test
    void getMe_returns401WithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isUnauthorized());
    }
}
