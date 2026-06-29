package com.surstudio.cts.assessment.application;

import com.surstudio.cts.assessment.domain.*;
import com.surstudio.cts.assessment.dto.*;
import com.surstudio.cts.common.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SkillTestServiceTest {

    @Mock SkillTestRepository skillTestRepository;
    @Mock QuestionRepository questionRepository;
    @InjectMocks SkillTestService service;

    @Test
    void createTest_persistsAndReturnsAdminResponse() {
        var request = new SkillTestRequest(Skill.ACROBACIA, "Test Básico", null);
        var saved = skillTestWith(1L, Skill.ACROBACIA, "Test Básico", true);
        when(skillTestRepository.save(any())).thenReturn(saved);

        var result = service.createTest(request);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.skill()).isEqualTo(Skill.ACROBACIA);
        assertThat(result.active()).isTrue();
        assertThat(result.questions()).isEmpty();
        verify(skillTestRepository).save(any(SkillTest.class));
    }

    @Test
    void createTest_respectsExplicitActiveFalse() {
        var request = new SkillTestRequest(Skill.RITMICA, "Y", false);
        var saved = skillTestWith(2L, Skill.RITMICA, "Y", false);
        when(skillTestRepository.save(any())).thenReturn(saved);

        var result = service.createTest(request);

        assertThat(result.active()).isFalse();
    }

    @Test
    void updateTest_throwsWhenNotFound() {
        when(skillTestRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateTest(99L, new SkillTestRequest(Skill.ACROBACIA, "Y", null)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void addQuestion_savesWithAllOptions() {
        var test = skillTestWith(1L, Skill.ACROBACIA, "T", true);
        var optReq = new OptionRequest("Impulso, vuelo y aterrizaje", true, 0);
        var qReq = new QuestionRequest("¿Cuántas fases?", 0, List.of(optReq));

        when(skillTestRepository.findById(1L)).thenReturn(Optional.of(test));
        when(questionRepository.save(any())).thenAnswer(inv -> {
            Question q = inv.getArgument(0);
            ReflectionTestUtils.setField(q, "id", 10L);
            q.getOptions().forEach(o -> ReflectionTestUtils.setField(o, "id", 20L));
            return q;
        });

        var result = service.addQuestion(1L, qReq);

        assertThat(result.id()).isEqualTo(10L);
        assertThat(result.text()).isEqualTo("¿Cuántas fases?");
        assertThat(result.options()).hasSize(1);
        assertThat(result.options().get(0).correct()).isTrue();
    }

    @Test
    void addQuestion_throwsWhenTestNotFound() {
        when(skillTestRepository.findById(5L)).thenReturn(Optional.empty());
        var qReq = new QuestionRequest("Q", 0, List.of(new OptionRequest("O", false, 0)));

        assertThatThrownBy(() -> service.addQuestion(5L, qReq))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("5");
    }

    @Test
    void deleteQuestion_throwsWhenNotFound() {
        when(questionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteQuestion(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void listActiveTests_returnsMappedCandidateViews() {
        var test = skillTestWith(1L, Skill.ACROBACIA, "Test", true);
        when(skillTestRepository.findByActiveTrue()).thenReturn(List.of(test));

        var result = service.listActiveTests();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).skill()).isEqualTo(Skill.ACROBACIA);
    }

    @Test
    void getTestForCandidate_throwsWhenNotFound() {
        when(skillTestRepository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getTestForCandidate(42L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("42");
    }

    // Trust boundary: SkillTestCandidateView.OptionDto has no correct() component.
    // This test compiles only if the record definition is correct — a structural guarantee.
    @Test
    void candidateView_optionDto_hasNoCorrectComponent() {
        var optionDto = new SkillTestCandidateView.OptionDto(1L, "Option A", 0);

        assertThat(optionDto.id()).isEqualTo(1L);
        assertThat(optionDto.text()).isEqualTo("Option A");
        assertThat(optionDto.position()).isEqualTo(0);
        // optionDto.correct() would be a compile error — the field does not exist
    }

    // --- helpers ---

    private SkillTest skillTestWith(Long id, Skill skill, String title, boolean active) {
        var t = new SkillTest();
        ReflectionTestUtils.setField(t, "id", id);
        t.setSkill(skill);
        t.setTitle(title);
        t.setActive(active);
        return t;
    }
}
