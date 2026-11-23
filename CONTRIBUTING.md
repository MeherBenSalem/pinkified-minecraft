# Contributing

Thanks for helping improve Pinkified Minecraft.

## How to contribute

1. Fork the repository and create a branch from `main`.
2. Make focused changes in the relevant Minecraft version workspace (`1.20.1/`, `1.21.1/`, `26.2/` or `26.3/`).
   A visual change usually belongs in all four: the `common/` code is nearly identical between them, and only the
   version-specific rendering and loader APIs differ.
3. Build the workspace you changed (`./gradlew build` from that folder: Gradle on Java 21 for 1.20.1 and 1.21.1,
   Java 25 for 26.x), or all four with `./gradlew buildAll` from the root (the 8 loader jars land in `releases/`).
4. Open a pull request describing the change and how you tested it.

## Layout

Each workspace is a MultiLoader Gradle build:

- `common/`: everything the player sees: the config, the themes and palettes, the texture recolouring pipeline,
  the main menu and settings screens, widgets, HUD overlays, particles and sounds. Nearly all changes happen here.
- `fabric/` and `neoforge/` (`forge/` on 1.20.1): entry points that connect the loader's events (or, where Fabric API
  has none, a few mixins) to the hook classes in `common/`, and the `PinkPlatform` implementation (config folder,
  key binding, mod list button, branding lines).

Version-specific code is kept in a few places: `client/gui/RenderKit` and the widgets (GUI drawing calls),
`client/texture` (how textures and atlases are uploaded), `client/particle` and the loader modules. On 26.x the
container and game-over hooks are shared mixins in `common/` (`pinkified.mixins.json`).

## Art and sound

Every texture is original pixel art drawn by `tools/assetgen/AssetGenerator.java`, and the lullaby and UI sounds are
synthesised by `tools/audiogen/AudioGenerator.java`; see the README for the commands. Do not add Mojang assets.

## Testing in game

`./gradlew :<loader>:runClient` in a workspace starts a dev client. On 1.20.1, `./gradlew :forge:runSnapshots` (or
`:fabric:runSnapshots`) visits every themed screen, saves screenshots and quits; CI runs it for both loaders. On 1.21.1
and 26.x, `PINKIFIED_SELFTEST=1 ./gradlew :<loader>:runClient` loads every mixin target, opens the settings screen and
quits; CI runs it for every loader. If you
change something visible, check it on every workspace you touched, for Fabric and NeoForge/Forge.

## Coding expectations

- Prefer small, reviewable PRs over large mixed changes.
- Match existing naming, package layout, and MultiLoader patterns.
- Keep the mod client-only: no registry entries, no networking, nothing a server needs.
- Respect other resource packs and mods: recolour what the active packs provide, and leave modded textures alone.
- Do not commit build output, IDE metadata, or local secrets.
- When fixing a bug, say which version/loaders you verified.

## Issues

Use GitHub Issues for bugs and feature requests. Include Minecraft version, mod loader, mod version, and steps to
reproduce when reporting bugs.

## Licensing

By contributing, you agree that your contributions are licensed under the Apache License, Version 2.0, unless you
state otherwise in the pull request.
