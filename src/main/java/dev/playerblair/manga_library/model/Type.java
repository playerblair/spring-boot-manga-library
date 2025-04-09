package dev.playerblair.manga_library.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum Type {
    MANGA("Manga"),
    MANHWA("Manhwa"),
    MANHUA("Manhua"),
    ONESHOT("One-shot"),
    DOUJINSHI("Doujinshi"),
    LIGHTNOVEL("Light Novel"),
    NOVEL("Novel");

    @JsonValue
    private final String label;

    Type(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @JsonCreator
    public static Type fromLabel(String label) {
        return Arrays.stream(Type.values())
                .filter(type -> type.getLabel().equals(label))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown label: " + label));
    }
}
