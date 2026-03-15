# Bomb Bell Copier

> A Fabric `1.21.11` client mod for Wynncraft that runs `/bombbell` and turns active bombs into a Discord-ready message.

## ✨ What It Does

Bomb Bell Copier is built for people who want to quickly move Wynncraft bomb info into Discord without manually retyping servers, timers, or role pings.

- Runs `/bombbell` for you
- Reads the live active bomb list from chat
- Matches bomb types to Discord role IDs
- Copies a clean message straight to your clipboard
- Supports both grouped and flat output styles

## 🧾 Example Output

### `grouped` (default)

```text
<@&515987791230795807>
AS1 [16m 58s]
EU1 [12m 1s]
EU6 [13m 17s]

<@&1403678665056849963>
EU1 [12m 40s]
```

### `flat`

```text
<@&515987791230795807> AS1 [16m 58s]
<@&1403678665056849963> EU1 [12m 40s]
```

## 🚀 Quick Start

1. Put the mod jar into your Minecraft profile's `mods` folder.
2. Launch the game once, then close it.
3. Open `config/bombbelldiscord.json`.
4. Add your Discord role IDs under `roleMappings`.
5. Launch Wynncraft and type `/bombcopy`.
6. Paste the copied result into Discord.

That is enough to get started.

## 🛠 Setup For First-Time Users

### 1. Install the mod

Place the jar in your Fabric `mods` folder.

### 2. Generate the config

Start Minecraft once with the mod installed, then close the game.  
This creates:

```text
config/bombbelldiscord.json
```

### 3. Add your role IDs

Open the config and fill in the role IDs you want pinged.

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

### 4. Use the command

In Wynncraft, type:

```text
/bombcopy
```

The mod will:

1. run `/bombbell`
2. wait for the live chat response
3. format the active bombs
4. copy the finished message to your clipboard

## 🎮 How To Use It

### Option 1: Command

Use:

```text
/bombcopy
```

This is the main and recommended way to use the mod.

### Option 2: Hotkey

The mod ships with no default hotkey bound.  
If you want one, set `exportKey` in the config.

Examples:

- `key.keyboard.f6`
- `key.keyboard.b`
- `key.mouse.middle`

Leave it as `key.keyboard.unknown` if you only want to use `/bombcopy`.

## ⚙ Config Guide

### Main settings

- `includeUnmappedBombs`
  If `false`, only bombs with configured role IDs will be exported.
- `showRemainingTime`
  If `true`, each server line includes `[time left]`.
- `outputStyle`
  Use `grouped` or `flat`.
- `exportKey`
  Translation key for the optional hotkey.
- `roleMappings`
  Maps bomb names to Discord role IDs.
- `commandTimeoutMs`
  How long the mod waits for `/bombbell` before giving up.
- `quietPeriodMs`
  How long the mod waits after the last relevant bomb line before finalizing the export.
- `noBombsMessage`
  What gets copied if Wynncraft reports no active bombs.

### Recommended public-use defaults

- `includeUnmappedBombs: false`
- `showRemainingTime: true`
- `outputStyle: "grouped"`
- `exportKey: "key.keyboard.unknown"`

## 🧠 Bomb Name Matching

The mod normalizes common bomb names automatically.

Examples:

- `Loot`
- `Chest Loot`
- `Loot Chest Bomb`

These all resolve to the same loot-chest mapping key.

It also normalizes names like:

- `Combat XP`
- `Profession XP`
- `Profession Speed`

## 📦 Building

To build the mod yourself:

```powershell
.\gradlew.bat clean test build
```

The built jar will be placed in:

```text
build/libs/
```

## 📝 Changelog

Version history lives in [CHANGELOG.md](CHANGELOG.md).

## ❤️ Notes

- This is a clipboard exporter, not a Discord bot.
- It does not post directly to Discord.
- It uses live `/bombbell` chat output as the source of truth for active bombs.
