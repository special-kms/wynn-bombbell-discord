# Changelog

## 0.3.0

- Added `outputStyle` config support with `grouped` and `flat` modes.
- Switched the default export key to unbound. You can now set `exportKey` in `bombbelldiscord.json`.
- Added more parser and formatter tests for duplicate bomb lines, `no bombs` responses, flat output, and hidden timers.
- Added an icon hook in `fabric.mod.json` so the mod can display a custom icon in loaders.
- Expanded setup docs for first-time users.

## 0.2.0

- Added `/bombcopy` so the mod can run `/bombbell` and copy a fresh Discord-ready export.
- Changed the default output to grouped role mentions, so each role is tagged once and matching servers are listed underneath it.
- Added remaining time to exports, for example `NA15 [19m 58s]`.
- Fixed live parsing for flattened `/bombbell` chat messages and loot-chest role matching.
