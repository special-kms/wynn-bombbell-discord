# Bombbell Discord Export

Client-side Fabric mod for Minecraft Java Edition Fabric `1.21.11` that runs `/bombbell`, reads the live Wynncraft bomb list from chat, and copies a Discord-ready message to your clipboard.

## What the mod does

- Runs `/bombbell` for you when you use `/bombcopy` or a configured hotkey.
- Reads the active bombs directly from the live chat response.
- Maps bomb types to Discord role mentions like `<@&123456789012345678>`.
- Copies the result to your clipboard in either `grouped` or `flat` format.
- Includes remaining time like `NA15 [19m 58s]` by default.

## First-time setup

1. Put the jar in your profile `mods` folder.
2. Launch the game once, then close it.
3. Open `config/bombbelldiscord.json`.
4. Fill in `roleMappings` with your Discord role IDs.
5. Leave `outputStyle` as `grouped` unless you prefer one line per entry.
6. If you want a hotkey, change `exportKey` to something like `key.keyboard.f6`.
7. Launch the game again and use `/bombcopy`.

## How to use it

1. Join Wynncraft.
2. Make sure your `roleMappings` are set in the config.
3. Type `/bombcopy` in chat.
4. The mod runs `/bombbell`, waits for the live response, and copies the formatted result to your clipboard.
5. Paste the clipboard contents into Discord.

If you set a hotkey in config, that key does the same thing as `/bombcopy`.

## Output formats

### `grouped` (default)

Tags each role once, then lists matching servers underneath it.

```text
<@&515987791230795807>
AS1 [16m 58s]
EU1 [12m 1s]
EU6 [13m 17s]

<@&1403678665056849963>
EU1 [12m 40s]
```

### `flat`

Outputs one line per bomb entry.

```text
<@&515987791230795807> AS1 [16m 58s]
<@&1403678665056849963> EU1 [12m 40s]
```

## Config reference

The mod creates `config/bombbelldiscord.json` on first launch.

Example:

```json
{
  "includeUnmappedBombs": false,
  "showRemainingTime": true,
  "outputStyle": "grouped",
  "exportKey": "key.keyboard.unknown",
  "roleMappings": {
    "combat xp bomb": "515987791230795807",
    "profession xp bomb": "534473481685827585",
    "profession speed bomb": "534473860863754261",
    "loot chest bomb": "1403678665056849963"
  },
  "commandTimeoutMs": 2500,
  "quietPeriodMs": 350,
  "noBombsMessage": "No active bombs were detected."
}
```

Field notes:

- `includeUnmappedBombs`: if `false`, only bombs with configured role IDs are exported.
- `showRemainingTime`: if `true`, adds `[time left]` after each server.
- `outputStyle`: use `grouped` or `flat`.
- `exportKey`: keyboard or mouse translation key. Examples: `key.keyboard.f6`, `key.keyboard.b`, `key.mouse.middle`. Leave `key.keyboard.unknown` for no hotkey.
- `roleMappings`: bomb name to Discord role ID map.
- `commandTimeoutMs`: how long to wait for `/bombbell` before giving up.
- `quietPeriodMs`: how long to wait after the last relevant line before finalizing the export.
- `noBombsMessage`: copied to clipboard when Wynncraft reports no active bombs.

## Supported bomb-name matching

The mod normalizes common bomb names automatically. For example:

- `Loot`, `Chest Loot`, and `Loot Chest Bomb` all resolve to the same loot-chest role key.
- `Combat XP`, `Profession XP`, and `Profession Speed` are normalized for config matching.

## Building

```powershell
.\gradlew.bat clean test build
```

The release jar will be placed in `build/libs/`.

## Release notes

See [CHANGELOG.md](CHANGELOG.md) for version history.

## Icon metadata

The mod icon is loaded from `src/main/resources/assets/bombbelldiscord/icon.png` via `fabric.mod.json`. To replace it, keep the file name the same and use a square PNG, ideally `128x128`.

## Contact metadata

Before publishing, add your real project links to the `contact` section in `fabric.mod.json`.

Recommended fields:

- `issues`: your bug-report tracker
- `sources`: your repository
- `homepage`: optional project page
