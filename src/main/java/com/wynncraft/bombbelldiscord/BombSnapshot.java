package com.wynncraft.bombbelldiscord;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public final class BombSnapshot {
    private final Instant capturedAt;
    private final Map<String, List<BombEntry>> bombsByServer;

    public BombSnapshot(Instant capturedAt, Map<String, List<BombEntry>> bombsByServer) {
        this.capturedAt = Objects.requireNonNull(capturedAt, "capturedAt");

        LinkedHashMap<String, List<BombEntry>> copy = new LinkedHashMap<>();
        for (Map.Entry<String, List<BombEntry>> entry : bombsByServer.entrySet()) {
            copy.put(
                Objects.requireNonNull(entry.getKey(), "server"),
                List.copyOf(new ArrayList<>(entry.getValue()))
            );
        }
        this.bombsByServer = Collections.unmodifiableMap(copy);
    }

    public static BombSnapshot empty(Instant capturedAt) {
        return new BombSnapshot(capturedAt, Map.of());
    }

    public Instant capturedAt() {
        return capturedAt;
    }

    public Map<String, List<BombEntry>> bombsByServer() {
        return bombsByServer;
    }

    public boolean isEmpty() {
        return bombsByServer.isEmpty();
    }

    public int serverCount() {
        return bombsByServer.size();
    }

    public int bombCount() {
        return bombsByServer.values().stream().mapToInt(List::size).sum();
    }

    @Override
    public String toString() {
        return "BombSnapshot{" +
            "capturedAt=" + capturedAt +
            ", bombsByServer=" + bombsByServer.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue().size())
                .collect(Collectors.joining(", ")) +
            '}';
    }
}
