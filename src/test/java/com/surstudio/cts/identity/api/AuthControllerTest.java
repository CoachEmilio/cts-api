package com.surstudio.cts.identity.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.surstudio.cts.common.ConflictException;
import com.surstudio.cts.common.security.JwtService;
import com.surstudio.cts.common.security.SecurityConfig;
import com.surstudio.cts.common.security.UserDetailsConfig;
import com.surstudio.cts.identity.application.AuthService;
import com.surstudio.cts.identity.domain.UserRepository;
import com.surstudio.cts.identity.dto.AuthResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, UserDetailsConfig.class})
class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean AuthService authService;
    @MockBean JwtService jwtService;
    @MockBean UserRepository userRepository;

    @Test
    void register_returns201WithToken() throws Exception {
        when(authService.register(any()))
                .thenReturn(new AuthResponse("jwt-token-here", "candidate", false));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"coach@test.com","password":"secret123"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("jwt-token-here"))
                .andExpect(jsonPath("$.role").value("candidate"))
                .andExpect(jsonPath("$.onboardingComplete").value(false));
    }

    @Test
    void register_returns409WhenEmailTaken() throws Exception {
        when(authService.register(any()))
                .thenThrow(new ConflictException("Email already registered: coach@test.com"));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"coach@test.com","password":"secret123"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void register_returns400WhenEmailInvalid() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"not-an-email","password":"secret123"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void register_returns400WhenPasswordTooShort() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"coach@test.com","password":"short"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void login_returns200WithToken() throws Exception {
        when(authService.login(any()))
                .thenReturn(new AuthResponse("jwt-token-here", "candidate", false));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"coach@test.com","password":"secret123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token-here"))
                .andExpect(jsonPath("$.role").value("candidate"));
    }

    @Test
    void login_returns400WhenBodyInvalid() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}
