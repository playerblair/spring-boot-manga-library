package dev.playerblair.manga_library.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum Genre {
    ACTION("Action"),
    ADVENTURE("Adventure"),
    AVANT_GARDE("Avant Garde"),
    AWARD_WINNING("Award Winning"),
    BOYS_LOVE("Boys Love"),
    COMEDY("Comedy"),
    DRAMA("Drama"),
    FANTASY("Fantasy"),
    GIRLS_LOVE("Girls Love"),
    GOURMET("Gourmet"),
    HORROR("Horror"),
    MYSTERY("Mystery"),
    ROMANCE("Romance"),
    SCI_FI("Sci-Fi"),
    SLICE_OF_LIFE("Slice of Life"),
    SPORTS("Sports"),
    SUPERNATURAL("Supernatural"),
    SUSPENSE("Suspense");

    @JsonValue
    private final String label;

    Genre(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @JsonCreator
    public static Genre fromLabel(String label) {
        return Arrays.stream(Genre.values())
                .filter(genre -> genre.getLabel().equals(label))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown label: " + label));
    }
}
