package com.wynncraft.bombbelldiscord;

import java.util.Objects;

public record BombEntry(String bombType, long remainingSeconds, String remainingLabel, String sourceLine) {
    public BombEntry {
        bombType = Objects.requireNonNull(bombType, "bombType").trim();
        remainingSeconds = Math.max(0L, remainingSeconds);
        remainingLabel = Objects.requireNonNullElse(remainingLabel, "").trim();
        sourceLine = Objects.requireNonNullElse(sourceLine, "").trim();
    }
}
