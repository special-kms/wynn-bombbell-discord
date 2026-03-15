package com.wynncraft.bombbelldiscord;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class BombSnapshotFormatterTest {
    private final BombSnapshotFormatter formatter = new BombSnapshotFormatter();

    @Test
    void formatsCompactMentionLines() {
        BombbellDiscordConfig config = new BombbellDiscordConfig();
        config.roleMappings = Map.of(
            BombTextNormalizer.normalizeBombTypeKey("Loot Chest Bomb"), "111",
            BombTextNormalizer.normalizeBombTypeKey("Combat XP Bomb"), "222"
        );
        config.sanitize();

        BombSnapshot snapshot = new BombSnapshot(
            Instant.parse("2026-03-13T22:45:00Z"),
            Map.of(
                "NA7", List.of(new BombEntry("Combat XP Bomb", 1172L, "19m 32s", "line")),
                "NA9", List.of(new BombEntry("Loot Chest Bomb", 490L, "8m 10s", "line"))
            )
        );

        String formatted = formatter.format(snapshot, config);

        assertEquals("<@&222>\nNA7 [19m 32s]\n\n<@&111>\nNA9 [8m 10s]", formatted);
    }

    @Test
    void formatsExactBombcopyOutputStyle() {
        BombbellDiscordConfig config = new BombbellDiscordConfig();
        config.roleMappings = Map.of(
            BombTextNormalizer.normalizeBombTypeKey("Combat XP Bomb"), "515987791230795807",
            BombTextNormalizer.normalizeBombTypeKey("Profession Speed Bomb"), "534473860863754261"
        );
        config.sanitize();

        BombSnapshot snapshot = new BombSnapshot(
            Instant.parse("2026-03-13T22:45:00Z"),
            Map.of("NA15", List.of(
                new BombEntry("Combat XP Bomb", 1198L, "19m 58s", "line"),
                new BombEntry("Profession Speed Bomb", 586L, "9m 46s", "line")
            ))
        );

        String formatted = formatter.format(snapshot, config);

        assertEquals("<@&515987791230795807>\nNA15 [19m 58s]\n\n<@&534473860863754261>\nNA15 [9m 46s]", formatted);
    }

    @Test
    void filtersUnmappedBombsWhenConfigured() {
        BombbellDiscordConfig config = new BombbellDiscordConfig();
        config.includeUnmappedBombs = false;
        config.roleMappings = Map.of(BombTextNormalizer.normalizeBombTypeKey("Loot Bomb"), "111");
        config.sanitize();

        BombSnapshot snapshot = new BombSnapshot(
            Instant.parse("2026-03-13T22:45:00Z"),
            Map.of("WC1", List.of(
                new BombEntry("Loot Bomb", 490L, "8m 10s", "line"),
                new BombEntry("Dungeon Bomb", 120L, "2m", "line")
            ))
        );

        String formatted = formatter.format(snapshot, config);
        assertTrue(formatted.contains("<@&111>\nWC1 [8m 10s]"));
        assertFalse(formatted.contains("Dungeon Bomb"));
    }

    @Test
    void groupsMultipleServersUnderOneRole() {
        BombbellDiscordConfig config = new BombbellDiscordConfig();
        config.roleMappings = Map.of(BombTextNormalizer.normalizeBombTypeKey("Combat XP Bomb"), "515987791230795807");
        config.sanitize();

        BombSnapshot snapshot = new BombSnapshot(
            Instant.parse("2026-03-13T22:45:00Z"),
            Map.of(
                "AS1", List.of(new BombEntry("Combat XP Bomb", 1018L, "16m 58s", "line")),
                "EU1", List.of(new BombEntry("Combat XP Bomb", 721L, "12m 1s", "line")),
                "EU6", List.of(new BombEntry("Combat XP Bomb", 797L, "13m 17s", "line"))
            )
        );

        String formatted = formatter.format(snapshot, config);

        assertEquals("<@&515987791230795807>\nAS1 [16m 58s]\nEU1 [12m 1s]\nEU6 [13m 17s]", formatted);
    }

    @Test
    void mapsChestLootRoleAcrossAliasVariants() {
        BombbellDiscordConfig config = new BombbellDiscordConfig();
        config.roleMappings = Map.of(BombTextNormalizer.normalizeBombTypeKey("chest loot bomb"), "1403678665056849963");
        config.sanitize();

        BombSnapshot snapshot = new BombSnapshot(
            Instant.parse("2026-03-13T22:45:00Z"),
            Map.of("EU1", List.of(new BombEntry("Loot Chest Bomb", 760L, "12m 40s", "line")))
        );

        String formatted = formatter.format(snapshot, config);

        assertEquals("<@&1403678665056849963>\nEU1 [12m 40s]", formatted);
    }

    @Test
    void supportsFlatOutputStyle() {
        BombbellDiscordConfig config = new BombbellDiscordConfig();
        config.outputStyle = "flat";
        config.roleMappings = Map.of(BombTextNormalizer.normalizeBombTypeKey("Combat XP Bomb"), "515987791230795807");
        config.sanitize();

        BombSnapshot snapshot = new BombSnapshot(
            Instant.parse("2026-03-13T22:45:00Z"),
            Map.of("AS1", List.of(new BombEntry("Combat XP Bomb", 1018L, "16m 58s", "line")))
        );

        String formatted = formatter.format(snapshot, config);

        assertEquals("<@&515987791230795807> AS1 [16m 58s]", formatted);
    }

    @Test
    void canHideRemainingTime() {
        BombbellDiscordConfig config = new BombbellDiscordConfig();
        config.showRemainingTime = false;
        config.roleMappings = Map.of(BombTextNormalizer.normalizeBombTypeKey("Combat XP Bomb"), "515987791230795807");
        config.sanitize();

        BombSnapshot snapshot = new BombSnapshot(
            Instant.parse("2026-03-13T22:45:00Z"),
            Map.of("AS1", List.of(new BombEntry("Combat XP Bomb", 1018L, "16m 58s", "line")))
        );

        String formatted = formatter.format(snapshot, config);

        assertEquals("<@&515987791230795807>\nAS1", formatted);
    }

    @Test
    void returnsConfiguredEmptyMessage() {
        BombbellDiscordConfig config = new BombbellDiscordConfig();
        config.noBombsMessage = "Nothing to export.";
        config.sanitize();

        String formatted = formatter.format(BombSnapshot.empty(Instant.parse("2026-03-13T22:45:00Z")), config);
        assertEquals("Nothing to export.", formatted);
    }
}
