package com.wynncraft.bombbelldiscord;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

public final class BombTextNormalizer {
    private static final Pattern FORMATTING_CODE_PATTERN = Pattern.compile("(?i)\u00A7[0-9A-FK-OR]");
    private static final Pattern CONTROL_PATTERN = Pattern.compile("[\\p{Cntrl}&&[^\r\n\t]]");
    private static final Pattern SPACE_PATTERN = Pattern.compile("\\s+");
    private static final Map<String, String> BOMB_NAME_ALIASES = createBombNameAliases();

    private BombTextNormalizer() {
    }

    public static List<String> splitLines(String text) {
        List<String> lines = new ArrayList<>();
        if (text == null || text.isBlank()) {
            return lines;
        }

        for (String rawLine : text.split("\\R")) {
            String cleaned = cleanLine(rawLine);
            if (!cleaned.isBlank()) {
                lines.add(cleaned);
            }
        }

        return lines;
    }

    public static String cleanLine(String rawLine) {
        if (rawLine == null) {
            return "";
        }

        String cleaned = FORMATTING_CODE_PATTERN.matcher(rawLine).replaceAll("");
        cleaned = cleaned.replace('\u00A0', ' ');
        cleaned = CONTROL_PATTERN.matcher(cleaned).replaceAll("");
        cleaned = cleaned
            .replace('•', '-')
            .replace('●', '-')
            .replace('▪', '-')
            .replace('▶', '-')
            .replace('»', ' ')
            .trim();
        cleaned = SPACE_PATTERN.matcher(cleaned).replaceAll(" ");
        return cleaned;
    }

    public static String canonicalBombType(String rawBombType) {
        String cleaned = cleanLine(rawBombType)
            .replaceAll("(?i)\\bbombs?\\b", "")
            .replaceAll("(?i)^active\\s+", "")
            .trim();

        if (cleaned.isBlank()) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        for (String word : cleaned.split(" ")) {
            if (word.isBlank()) {
                continue;
            }
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append(normalizeWord(word));
        }
        builder.append(" Bomb");
        return builder.toString();
    }

    public static String canonicalBombName(String rawBombName) {
        String cleaned = cleanLine(rawBombName);
        if (cleaned.isBlank()) {
            return "";
        }

        String alias = BOMB_NAME_ALIASES.get(cleaned.toLowerCase(Locale.ROOT));
        if (alias != null) {
            return alias;
        }

        if (cleaned.toLowerCase(Locale.ROOT).endsWith(" bomb") || cleaned.toLowerCase(Locale.ROOT).endsWith(" bombs")) {
            return canonicalBombType(cleaned);
        }

        return canonicalBombType(cleaned + " Bomb");
    }

    public static String normalizeBombTypeKey(String rawBombType) {
        return canonicalBombName(rawBombType).toLowerCase(Locale.ROOT);
    }

    public static String canonicalServer(String rawServer) {
        String cleaned = cleanLine(rawServer);
        if (cleaned.isBlank()) {
            return "";
        }

        String upper = cleaned.toUpperCase(Locale.ROOT).replace(" ", "");
        if (upper.startsWith("WORLD")) {
            upper = upper.replace("WORLD", "WC");
        }
        if (upper.startsWith("SERVER")) {
            upper = upper.replace("SERVER", "SRV");
        }
        upper = upper.replace("-", "");

        if (upper.startsWith("WC")) {
            String suffix = upper.substring(2).replaceAll("\\D", "");
            return suffix.isBlank() ? "WC" : "WC" + suffix;
        }
        if (upper.startsWith("SRV")) {
            String suffix = upper.substring(3).replaceAll("\\D", "");
            return suffix.isBlank() ? "SRV" : "SRV" + suffix;
        }
        if (upper.matches("[A-Z]{2,}\\d+")) {
            return upper;
        }
        return cleaned;
    }

    private static String normalizeWord(String word) {
        String trimmed = word.trim();
        if (trimmed.equalsIgnoreCase("xp")) {
            return "XP";
        }
        if (trimmed.equalsIgnoreCase("wc")) {
            return "WC";
        }
        if (trimmed.length() == 1) {
            return trimmed.toUpperCase(Locale.ROOT);
        }
        return trimmed.substring(0, 1).toUpperCase(Locale.ROOT) + trimmed.substring(1).toLowerCase(Locale.ROOT);
    }

    private static Map<String, String> createBombNameAliases() {
        LinkedHashMap<String, String> aliases = new LinkedHashMap<>();
        aliases.put("combat xp", "Combat XP Bomb");
        aliases.put("profession xp", "Profession XP Bomb");
        aliases.put("profession speed", "Profession Speed Bomb");
        aliases.put("dungeon", "Dungeon Bomb");
        aliases.put("world event", "World Event Bomb");
        aliases.put("chest loot", "Loot Chest Bomb");
        aliases.put("chest loot bomb", "Loot Chest Bomb");
        aliases.put("loot chest", "Loot Chest Bomb");
        aliases.put("loot chest bomb", "Loot Chest Bomb");
        aliases.put("loot", "Loot Chest Bomb");
        return aliases;
    }
}
