package com.surstudio.cts.common;

import com.surstudio.cts.assessment.domain.Skill;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class SkillConverter implements Converter<String, Skill> {
    @Override
    public Skill convert(String source) {
        return Skill.fromValue(source);
    }
}