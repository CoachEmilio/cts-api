package com.surstudio.cts.assessment.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;
import java.util.stream.Collectors;

public enum Skill {
    ACROBACIA,
    ARTISTICA,
    RITMICA,
    TRAMPOLIN,
    TUMBLING,
    AEROBICA,
    TELA;

    @JsonValue
    public String toJson() {
        return name().toLowerCase();
    }

    @JsonCreator
    public static Skill fromValue(String value) {
        if (value == null || value.isBlank()) {
            String valid = Arrays.stream(values()).map(Skill::toJson).collect(Collectors.joining(", "));
            throw new IllegalArgumentException("Skill must not be blank. Valid values: " + valid);
        }
        try {
            return valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            String valid = Arrays.stream(values()).map(Skill::toJson).collect(Collectors.joining(", "));
            throw new IllegalArgumentException(
                    "Invalid skill '" + value + "'. Valid values: " + valid);
        }
    }
}