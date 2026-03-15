package com.wynncraft.bombbelldiscord;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class BombSnapshotFormatter {
    private static final Comparator<String> SERVER_COMPARATOR = Comparator
        .comparingInt(BombSnapshotFormatter::serverPrefixOrder)
        .thenComparing(BombSnapshotFormatter::extractServerNumber)
        .thenComparing(Comparator.naturalOrder());

    public BombSnapshotFormatter() {
    }

    public String format(BombSnapshot snapshot, BombbellDiscordConfig config) {
        config.sanitize();

        Map<String, List<BombEntry>> filtered = filterBombs(snapshot.bombsByServer(), config);
        if (filtered.isEmpty()) {
            return config.noBombsMessage;
        }

        return config.useGroupedOutput() ? formatGrouped(filtered, config) : formatFlat(filtered, config);
    }

    private String formatGrouped(Map<String, List<BombEntry>> filtered, BombbellDiscordConfig config) {
        LinkedHashMap<String, List<String>> grouped = new LinkedHashMap<>();
        List<String> servers = new ArrayList<>(filtered.keySet());
        servers.sort(SERVER_COMPARATOR);
        for (String server : servers) {
            for (BombEntry entry : filtered.get(server)) {
                String mention = config.renderRoleMention(entry.bombType());
                String prefix = mention.isBlank() ? entry.bombType() : mention;
                grouped.computeIfAbsent(prefix, ignored -> new ArrayList<>())
                    .add(formatServerLine(server, entry, config));
            }
        }

        List<String> lines = new ArrayList<>();
        boolean firstGroup = true;
        for (Map.Entry<String, List<String>> entry : grouped.entrySet()) {
            if (!firstGroup) {
                lines.add("");
            }
            firstGroup = false;
            lines.add(entry.getKey());
            lines.addAll(entry.getValue());
        }

        return String.join("\n", lines);
    }

    private String formatFlat(Map<String, List<BombEntry>> filtered, BombbellDiscordConfig config) {
        List<String> lines = new ArrayList<>();
        List<String> servers = new ArrayList<>(filtered.keySet());
        servers.sort(SERVER_COMPARATOR);
        for (String server : servers) {
            for (BombEntry entry : filtered.get(server)) {
                String mention = config.renderRoleMention(entry.bombType());
                String prefix = mention.isBlank() ? entry.bombType() : mention;
                lines.add(prefix + " " + formatServerLine(server, entry, config));
            }
        }

        return String.join("\n", lines);
    }

    private Map<String, List<BombEntry>> filterBombs(Map<String, List<BombEntry>> bombsByServer, BombbellDiscordConfig config) {
        LinkedHashMap<String, List<BombEntry>> filtered = new LinkedHashMap<>();
        for (Map.Entry<String, List<BombEntry>> entry : bombsByServer.entrySet()) {
            List<BombEntry> kept = entry.getValue().stream()
                .filter(bomb -> config.includeUnmappedBombs || !config.findRoleId(bomb.bombType()).isBlank())
                .sorted(Comparator.comparingLong(BombEntry::remainingSeconds).reversed().thenComparing(BombEntry::bombType))
                .toList();
            if (!kept.isEmpty()) {
                filtered.put(entry.getKey(), kept);
            }
        }
        return filtered;
    }

    private String formatServerLine(String server, BombEntry entry, BombbellDiscordConfig config) {
        if (!config.showRemainingTime) {
            return server;
        }
        return server + " [" + entry.remainingLabel() + "]";
    }

    private static int serverPrefixOrder(String server) {
        if (server.startsWith("WC")) {
            return 0;
        }
        if (server.startsWith("SRV")) {
            return 1;
        }
        return 2;
    }

    private static int extractServerNumber(String server) {
        String digits = server.replaceAll("\\D", "");
        return digits.isBlank() ? Integer.MAX_VALUE : Integer.parseInt(digits);
    }
}
