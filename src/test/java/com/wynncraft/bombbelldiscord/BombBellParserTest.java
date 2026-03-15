package com.wynncraft.bombbelldiscord;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class BombBellParserTest {
    private final BombBellParser parser = new BombBellParser();

    @Test
    void parsesBombbellCommandLines() {
        BombSnapshot snapshot = parser.parse(
            Instant.parse("2026-03-13T22:30:00Z"),
            List.of(
                "Bombs:",
                "Combat XP on NA7 for: 19m 32s",
                "Loot on NA9 for: 8m 10s"
            )
        ).orElseThrow();

        assertEquals(2, snapshot.serverCount());
        BombEntry combatXp = snapshot.bombsByServer().get("NA7").getFirst();
        BombEntry loot = snapshot.bombsByServer().get("NA9").getFirst();
        assertEquals("Combat XP Bomb", combatXp.bombType());
        assertEquals("19m 32s", combatXp.remainingLabel());
        assertEquals(1172L, combatXp.remainingSeconds());
        assertEquals("Loot Chest Bomb", loot.bombType());
    }

    @Test
    void parsesFlattenedBombbellChatMessage() {
        BombSnapshot snapshot = parser.parse(
            Instant.parse("2026-03-13T22:30:00Z"),
            List.of("Bombs: Combat XP on NA15 for: 19m 58s Profession Speed on NA15 for: 09m 46s")
        ).orElseThrow();

        assertEquals(1, snapshot.serverCount());
        assertEquals(2, snapshot.bombsByServer().get("NA15").size());
        assertEquals("Combat XP Bomb", snapshot.bombsByServer().get("NA15").get(0).bombType());
        assertEquals("Profession Speed Bomb", snapshot.bombsByServer().get("NA15").get(1).bombType());
    }

    @Test
    void ignoresExpiredOrMalformedLines() {
        BombSnapshot snapshot = parser.parse(
            Instant.parse("2026-03-13T22:30:00Z"),
            List.of(
                "Bombs:",
                "Combat XP on NA7 for: 0s",
                "Loot on NA9 for: ???",
                "Profession XP on EU2 for: 1h 2m"
            )
        ).orElseThrow();

        assertEquals(1, snapshot.serverCount());
        assertEquals("Profession XP Bomb", snapshot.bombsByServer().get("EU2").getFirst().bombType());
    }

    @Test
    void keepsLatestDuplicateBombOnSameServer() {
        BombSnapshot snapshot = parser.parse(
            Instant.parse("2026-03-13T22:30:00Z"),
            List.of(
                "Bombs:",
                "Combat XP on NA7 for: 19m 32s",
                "Combat XP on NA7 for: 18m 50s"
            )
        ).orElseThrow();

        assertEquals(1, snapshot.serverCount());
        assertEquals(1, snapshot.bombsByServer().get("NA7").size());
        assertEquals("18m 50s", snapshot.bombsByServer().get("NA7").getFirst().remainingLabel());
    }

    @Test
    void returnsEmptyForHeaderWithNoBombsLine() {
        assertTrue(parser.parse(
            Instant.parse("2026-03-13T22:30:00Z"),
            List.of("Bombs:", "No active bombs.")
        ).isEmpty());
    }

    @Test
    void returnsEmptyWhenNothingRelevantExists() {
        assertTrue(parser.parse(Instant.parse("2026-03-13T22:30:00Z"), List.of("Hello world")).isEmpty());
    }
}
