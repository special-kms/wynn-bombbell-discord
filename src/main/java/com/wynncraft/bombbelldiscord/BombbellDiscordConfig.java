package com.wynncraft.bombbelldiscord;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public final class BombbellDiscordConfig {
    public String headerTemplate = "Wynncraft Bomb Report";
    public boolean timestampEnabled = true;
    public boolean includeUnmappedBombs = false;
    public boolean showRemainingTime = true;
    public String outputStyle = "grouped";
    public String exportKey = "key.keyboard.unknown";
    public Map<String, String> roleMappings = new LinkedHashMap<>();
    public int commandTimeoutMs = 2500;
    public int quietPeriodMs = 350;
    public String noBombsMessage = "No active bombs were detected.";

    public void sanitize() {
        if (headerTemplate == null) {
            headerTemplate = "Wynncraft Bomb Report";
        }
        if (noBombsMessage == null || noBombsMessage.isBlank()) {
            noBombsMessage = "No active bombs were detected.";
        }
        if (!"grouped".equalsIgnoreCase(outputStyle) && !"flat".equalsIgnoreCase(outputStyle)) {
            outputStyle = "grouped";
        } else {
            outputStyle = outputStyle.toLowerCase(Locale.ROOT);
        }
        if (exportKey == null || exportKey.isBlank()) {
            exportKey = "key.keyboard.unknown";
        }
        if (commandTimeoutMs < 500) {
            commandTimeoutMs = 500;
        }
        if (quietPeriodMs < 100) {
            quietPeriodMs = 100;
        }
        if (roleMappings == null) {
            roleMappings = new LinkedHashMap<>();
        }

        LinkedHashMap<String, String> normalized = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : roleMappings.entrySet()) {
            String normalizedKey = BombTextNormalizer.normalizeBombTypeKey(entry.getKey());
            String roleId = entry.getValue() == null ? "" : entry.getValue().trim();
            if (!normalizedKey.isBlank() && !roleId.isBlank()) {
                normalized.put(normalizedKey, roleId);
            }
        }
        roleMappings = normalized;
    }

    public String findRoleId(String bombType) {
        String key = BombTextNormalizer.normalizeBombTypeKey(bombType);
        return roleMappings.getOrDefault(key, "");
    }

    public String renderRoleMention(String bombType) {
        String roleId = findRoleId(bombType);
        return roleId.isBlank() ? "" : "<@&" + roleId + ">";
    }

    public String normalizedHeaderTemplate() {
        return headerTemplate == null ? "" : headerTemplate.trim();
    }

    public boolean useGroupedOutput() {
        return !"flat".equals(outputStyle);
    }

    @Override
    public String toString() {
        return "BombbellDiscordConfig{" +
            "headerTemplate='" + headerTemplate + '\'' +
            ", timestampEnabled=" + timestampEnabled +
            ", includeUnmappedBombs=" + includeUnmappedBombs +
            ", showRemainingTime=" + showRemainingTime +
            ", outputStyle='" + outputStyle + '\'' +
            ", exportKey='" + exportKey + '\'' +
            ", roleMappings=" + roleMappings.keySet().stream().map(key -> key.toLowerCase(Locale.ROOT)).toList() +
            '}';
    }
}
