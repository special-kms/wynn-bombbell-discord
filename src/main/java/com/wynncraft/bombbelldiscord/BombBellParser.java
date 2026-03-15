package com.wynncraft.bombbelldiscord;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class BombBellParser {
    private static final Pattern BOMB_LINE_PATTERN =
        Pattern.compile("(?i)(.+?)\\s+on\\s+([A-Z]{2,}\\d+)\\s+for:\\s*((?:(?:\\d+)h\\s*)?(?:(?:\\d+)m\\s*)?(?:(?:\\d+)s\\s*)?)");
    private static final Pattern DURATION_PATTERN =
        Pattern.compile("(?i)^\\s*(?:(\\d+)h)?\\s*(?:(\\d+)m)?\\s*(?:(\\d+)s)?\\s*$");

    public Optional<BombSnapshot> parse(Instant capturedAt, Collection<String> rawLines) {
        LinkedHashMap<String, LinkedHashMap<String, BombEntry>> entriesByServer = new LinkedHashMap<>();

        for (String rawLine : rawLines) {
            String line = BombTextNormalizer.cleanLine(rawLine);
            if (line.isBlank()) {
                continue;
            }

            List<ParsedBombLine> parsedLines = parseBombLines(line);
            if (parsedLines.isEmpty()) {
                continue;
            }
            for (ParsedBombLine parsed : parsedLines) {
                LinkedHashMap<String, BombEntry> entries = entriesByServer.computeIfAbsent(parsed.server(), ignored -> new LinkedHashMap<>());
                entries.put(parsed.bombType(), new BombEntry(parsed.bombType(), parsed.remainingSeconds(), parsed.remainingLabel(), line));
            }
        }

        if (entriesByServer.isEmpty()) {
            return Optional.empty();
        }

        LinkedHashMap<String, List<BombEntry>> finalized = new LinkedHashMap<>();
        for (Map.Entry<String, LinkedHashMap<String, BombEntry>> entry : entriesByServer.entrySet()) {
            finalized.put(entry.getKey(), List.copyOf(entry.getValue().values()));
        }

        return Optional.of(new BombSnapshot(capturedAt, finalized));
    }

    private List<ParsedBombLine> parseBombLines(String line) {
        String cleaned = BombTextNormalizer.cleanLine(line).replaceFirst("(?i)^bombs:\\s*", "");
        Matcher matcher = BOMB_LINE_PATTERN.matcher(cleaned);
        LinkedHashSet<ParsedBombLine> parsedLines = new LinkedHashSet<>();
        while (matcher.find()) {
            String bombType = BombTextNormalizer.canonicalBombName(matcher.group(1));
            String server = BombTextNormalizer.canonicalServer(matcher.group(2));
            DurationParse duration = parseDuration(matcher.group(3));
            if (bombType.isBlank() || server.isBlank() || duration == null || duration.remainingSeconds() <= 0) {
                continue;
            }
            parsedLines.add(new ParsedBombLine(bombType, server, duration.remainingSeconds(), duration.remainingLabel()));
        }
        return List.copyOf(parsedLines);
    }

    private DurationParse parseDuration(String rawDuration) {
        String cleaned = BombTextNormalizer.cleanLine(rawDuration);
        Matcher matcher = DURATION_PATTERN.matcher(cleaned);
        if (!matcher.matches()) {
            return null;
        }

        long hours = parseLong(matcher.group(1));
        long minutes = parseLong(matcher.group(2));
        long seconds = parseLong(matcher.group(3));
        long totalSeconds = (hours * 3600L) + (minutes * 60L) + seconds;
        if (totalSeconds <= 0) {
            return null;
        }
        return new DurationParse(totalSeconds, formatDuration(totalSeconds));
    }

    private long parseLong(String value) {
        return value == null || value.isBlank() ? 0L : Long.parseLong(value);
    }

    private String formatDuration(long totalSeconds) {
        long hours = totalSeconds / 3600L;
        long minutes = (totalSeconds % 3600L) / 60L;
        long seconds = totalSeconds % 60L;

        List<String> parts = new ArrayList<>();
        if (hours > 0L) {
            parts.add(hours + "h");
        }
        if (minutes > 0L) {
            parts.add(minutes + "m");
        }
        if (seconds > 0L || parts.isEmpty()) {
            parts.add(seconds + "s");
        }
        return String.join(" ", parts);
    }

    private record ParsedBombLine(String bombType, String server, long remainingSeconds, String remainingLabel) {
    }

    private record DurationParse(long remainingSeconds, String remainingLabel) {
    }
}
