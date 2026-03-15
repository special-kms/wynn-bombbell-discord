package com.wynncraft.bombbelldiscord;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public final class BombCaptureService {
    private final BombbellDiscordConfigManager configManager;
    private final BombBellParser parser;
    private final BombSnapshotFormatter formatter;
    private final List<String> pendingCommandLines = new ArrayList<>();

    private BombSnapshot latestSnapshot = BombSnapshot.empty(Instant.EPOCH);
    private boolean awaitingCommandResponse;
    private boolean sawBombbellResponse;
    private boolean sawParsableBombLine;
    private boolean sawExplicitNoBombsLine;
    private Instant requestStartedAt = Instant.EPOCH;
    private Instant lastRelevantLineAt = Instant.EPOCH;

    public BombCaptureService(
        BombbellDiscordConfigManager configManager,
        BombBellParser parser,
        BombSnapshotFormatter formatter
    ) {
        this.configManager = configManager;
        this.parser = parser;
        this.formatter = formatter;
    }

    public synchronized void captureChatText(String text) {
        if (!awaitingCommandResponse) {
            return;
        }

        Instant now = Instant.now();
        for (String line : BombTextNormalizer.splitLines(text)) {
            if (!isPotentialBombbellLine(line)) {
                continue;
            }

            pendingCommandLines.add(line);
            sawBombbellResponse = true;
            sawParsableBombLine = sawParsableBombLine || parser.parse(now, pendingCommandLines).isPresent();
            sawExplicitNoBombsLine = sawExplicitNoBombsLine || isExplicitNoBombsLine(line);
            lastRelevantLineAt = now;
        }
    }

    public synchronized void captureScreenText(String text) {
        // Command-driven export does not rely on HUD/sidebar scraping.
    }

    public synchronized void captureTooltipText(List<Text> tooltipLines) {
        // Command-driven export does not rely on tooltip scraping.
    }

    public synchronized BombSnapshot latestSnapshot() {
        return latestSnapshot;
    }

    public synchronized void requestFreshExport(MinecraftClient client) {
        if (awaitingCommandResponse) {
            sendStatus(client, "Already waiting for /bombbell output.");
            return;
        }
        if (client.player == null || client.player.networkHandler == null) {
            sendStatus(client, "You must be in game to export Bomb Bell data.");
            return;
        }

        BombbellDiscordConfig config = configManager.get();
        config.sanitize();

        pendingCommandLines.clear();
        awaitingCommandResponse = true;
        sawBombbellResponse = false;
        sawParsableBombLine = false;
        sawExplicitNoBombsLine = false;
        requestStartedAt = Instant.now();
        lastRelevantLineAt = requestStartedAt;

        client.player.networkHandler.sendChatCommand("bombbell");
        sendStatus(client, "Running /bombbell...");
    }

    public synchronized void tick(MinecraftClient client) {
        if (!awaitingCommandResponse) {
            return;
        }

        BombbellDiscordConfig config = configManager.get();
        Instant now = Instant.now();
        long elapsedMs = now.toEpochMilli() - requestStartedAt.toEpochMilli();
        long quietMs = now.toEpochMilli() - lastRelevantLineAt.toEpochMilli();
        boolean timedOut = elapsedMs >= config.commandTimeoutMs;
        boolean quietEnough = (sawParsableBombLine || sawExplicitNoBombsLine) && quietMs >= config.quietPeriodMs;

        if (timedOut || quietEnough) {
            finalizeCommandResponse(client, now);
        }
    }

    private void finalizeCommandResponse(MinecraftClient client, Instant now) {
        awaitingCommandResponse = false;

        BombbellDiscordConfig config = configManager.get();
        Optional<BombSnapshot> snapshot = parser.parse(now, pendingCommandLines);
        if (snapshot.isPresent()) {
            latestSnapshot = snapshot.get();
            client.keyboard.setClipboard(formatter.format(latestSnapshot, config));
            sendStatus(client, "Copied " + latestSnapshot.bombCount() + " active bomb entries across " + latestSnapshot.serverCount() + " servers.");
        } else if (sawExplicitNoBombsLine) {
            client.keyboard.setClipboard(config.noBombsMessage);
            latestSnapshot = BombSnapshot.empty(now);
            sendStatus(client, "No active bombs were reported by /bombbell.");
        } else {
            sendStatus(client, "Did not receive a usable /bombbell response.");
        }

        pendingCommandLines.clear();
        sawBombbellResponse = false;
        sawParsableBombLine = false;
        sawExplicitNoBombsLine = false;
    }

    private boolean isPotentialBombbellLine(String line) {
        String lower = line.toLowerCase();
        return lower.equals("bombs:")
            || lower.contains(" on ")
            || lower.contains(" for:")
            || lower.contains("no active bombs")
            || lower.contains("no bombs");
    }

    private boolean isExplicitNoBombsLine(String line) {
        String lower = line.toLowerCase();
        return lower.contains("no active bombs") || lower.contains("no bombs");
    }

    private void sendStatus(MinecraftClient client, String message) {
        if (client.player != null) {
            client.player.sendMessage(Text.literal(message), true);
        }
    }
}
