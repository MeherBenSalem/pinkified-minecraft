# Pinkified Minecraft 1.0.0: release files

One jar, for Minecraft 1.20.1 on Forge. Install it on the client only; servers never need it, and players with it can join any server. On CurseForge, tick Minecraft 1.20.1, Forge, Client and Java 17. It needs no other mods.

| File | Minecraft | Loader | Java | Side | Required dependencies |
|---|---|---|---|---|---|
| `pinkified-forge-1.20.1-1.0.0.jar` | 1.20.1 | Forge 47 or newer | 17 | Client | None |

Built by the Build release jar workflow (`.github/workflows/build-release.yml`), which runs `./gradlew build` and commits the jar here only after CI passed on the same commit:

- **Jar checks:** the file name, `Implementation-Version` and the `mods.toml` version are 1.0.0, `mods.toml` keeps `clientSideOnly=true`, the menu music and mod-list logo are packed, and the classes are SRG-mapped for Forge.
- **Server boot:** a real Forge 1.20.1 dedicated server with this jar in its `mods` folder starts, runs for 20 seconds and stops, with no crash and no error naming the mod (`scripts/server-boot-test.sh`).
- **Client launch:** a dev client starts under Xvfb at 1920×1080, walks through the main menu in all seven themes, the five settings tabs, the options and multiplayer screens, a fresh world's HUD and every themed container, pause and game-over screen, takes its 31 screenshots and quits, with no crash and no error naming the mod (`./gradlew runSnapshots`).
- **No GameTests:** Pinkified is client-only and registers nothing on the server side, so there is nothing for GameTests to check.

The developer screenshot harness (`client/dev/DevSnapshots.java`) is in the jar, because the client code wires it up, but it does nothing unless the game is started with `-Dpinkified.devshots=true`.
