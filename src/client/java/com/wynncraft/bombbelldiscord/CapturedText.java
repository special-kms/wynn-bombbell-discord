package com.wynncraft.bombbelldiscord;

import java.time.Instant;

record CapturedText(TextOrigin origin, String line, Instant capturedAt) {
}
