package com.surstudio.cts.onboarding.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.surstudio.cts.common.security.JwtService;
import com.surstudio.cts.common.security.SecurityConfig;
import com.surstudio.cts.common.security.UserDetailsConfig;
import com.surstudio.cts.identity.domain.UserRepository;
import com.surstudio.cts.onboarding.application.OnboardingService;
import com.surstudio.cts.onboarding.dto.GeneralInfoResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GeneralInfoController.class)
@Import({SecurityConfig.class, UserDetailsConfig.class})
class GeneralInfoControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean OnboardingService onboardingService;
    @MockBean JwtService jwtService;
    @MockBean UserRepository userRepository;

    @Test
    @WithMockUser(roles = "CANDIDATE")
    void getGeneralInfo_returnsCategoryList() throws Exception {
        var optionDto = new GeneralInfoResponse.OptionDto(1L, "Gimnasia Artística");
        var questionDto = new GeneralInfoResponse.QuestionDto(1L, "¿Especialidad?", "SINGLE", List.of(optionDto));
        var category = new GeneralInfoResponse(1L, "Especialidad", List.of(questionDto));
        when(onboardingService.getGeneralInfo()).thenReturn(List.of(category));

        mockMvc.perform(get("/api/v1/general-information"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Especialidad"))
                .andExpect(jsonPath("$[0].questions[0].question").value("¿Especialidad?"))
                .andExpect(jsonPath("$[0].questions[0].answers[0].answer").value("Gimnasia Artística"));
    }

    @Test
    @WithMockUser(roles = "CANDIDATE")
    void saveGeneralInfo_returns204() throws Exception {
        doNothing().when(onboardingService).saveGeneralInfo(any(), any());

        mockMvc.perform(post("/api/v1/general-information")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"answers":[{"questionId":1,"answerId":2}]}
                                """))
                .andExpect(status().isNoContent());
    }

    @Test
    void getGeneralInfo_returns401WithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/general-information"))
                .andExpect(status().isUnauthorized());
    }
}
