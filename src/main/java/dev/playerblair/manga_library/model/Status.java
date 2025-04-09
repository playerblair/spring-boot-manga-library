package dev.playerblair.manga_library.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum Status {
    PUBLISHING("Publishing"),
    FINISHED("Finished"),
    HIATUS("On Hiatus"),
    DISCONTINUED("Discontinued"),
    UPCOMING("Upcoming");

    @JsonValue
    private final String label;

    Status(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @JsonCreator
    public static Status fromLabel(String label) {
        return Arrays.stream(Status.values())
                .filter(status -> status.getLabel().equals(label))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown label: " + label));
    }
}
