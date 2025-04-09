package dev.playerblair.manga_library.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum ProgressType {
    PLANNING("Planning"),
    READING("Reading"),
    FINISHED("Finished"),
    PAUSED("Paused"),
    DROPPED("Dropping");

    @JsonValue
    private final String label;

    ProgressType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @JsonCreator
    public static ProgressType fromLabel(String label) {
        return Arrays.stream(ProgressType.values())
                .filter(progressType -> progressType.getLabel().equals(label))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown label: " + label));
    }
}
