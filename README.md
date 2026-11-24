# Pinkified Minecraft

**The Complete Pink Overhaul**, part of the Sugarplum Studios collection.

Pinkified turns Minecraft's interface into a soft pastel world (coquette pinks, strawberry milk,
lavender, cream, bows, hearts, petals and sparkles) without touching gameplay. It is a
**client-side-only** mod: servers never need it, and you can join any server with it installed.

## Which jar do I download?

Jar names are `pinkified-{loader}-{minecraft}-{modVersion}.jar`. Match **both** the Minecraft version and the
loader.

| Minecraft | Java | Fabric jar (needs Fabric API) | NeoForge / Forge jar |
|-----------|------|-------------------------------|----------------------|
| 1.20.1 | 17+ | `…-fabric-1.20.1-….jar` | `…-forge-1.20.1-….jar` (Forge 47) |
| 1.21.1 | 21+ | `…-fabric-1.21.1-….jar` | `…-neoforge-1.21.1-….jar` |
| 26.2 | 25+ | `…-fabric-26.2-….jar` | `…-neoforge-26.2-….jar` |
| 26.3 | 25+ | `…-fabric-26.3-….jar` | `…-neoforge-26.3-….jar` |

The mod is only needed on the client.

## Features

### Themes
Six hand-tuned themes plus a custom one. A theme recolours *everything* at once: menus, containers,
HUD icons, particles, tooltips, the enchantment glint and the main menu sky.

| Theme | Feel | Hunger treat |
|---|---|---|
| Strawberry Milk | milky pink, strawberry red, cream | strawberries |
| Lavender Dreams | lilac and purple with pink accents | macarons |
| Pink Coquette | baby pink, ivory, ribbon red | donuts |
| Cherry Blossom | sakura pink, white, spring green | dango |
| Angelcore | cream, white, gold, sky blue | cupcakes |
| Pastel Princess | pink-lilac with gold | bonbons |
| Custom | any two colours (hue and softness sliders) | macarons |

Switching themes is instant (no resource reload).

### Interface
* **Main menu**: animated pastel sky (drifting clouds, soft bokeh light, falling petals, twinkling
  sparkles), the Minecraft logo recoloured in theme colours with a "Pinkified" ribbon, cute splash
  texts, animated rounded buttons (lift, glow, hover heart, press squish, staggered slide-in), fade-in,
  the loader's mod list (Forge/NeoForge, or Mod Menu on Fabric when it is installed), language and
  accessibility buttons and a heart button for Pinkified settings.
* **Containers**: inventory, crafting table, furnace/blast furnace/smoker, anvil, enchanting table,
  chests, shulker boxes, hopper, dispenser, brewing stand, beacon, grindstone, loom, stonecutter,
  cartography/smithing tables, villager trading, horse inventory, creative inventory, recipe book and
  advancements are recoloured, with a little bow and heart tied onto each window.
* **Widgets**: buttons, sliders, checkboxes, server/world/resource-pack list icons, toasts and tabs are
  recoloured, and buttons and the hotbar get softly rounded corners. This also applies to other mods'
  screens that use vanilla widgets.
* **Menu backgrounds**: dirt is replaced by the animated pastel sky outside worlds, and in-game menus
  get a soft tint. Server, world and option lists sit on rounded translucent panels.
* **Pause menu**: tinted backdrop and a heart button that opens the settings.
* **Game-over screen**: a gentle pastel screen with drifting hearts instead of the red one.
* **Tooltips** in theme colours (tooltips that other mods colour themselves are left alone; on 26.x the
  tooltip background sprites are themed instead).
* **Soft transitions**: menus fade in gently.

### HUD
* Hearts (normal, hardcore, absorption, poison, wither, frozen, mount), air bubbles
* Hunger treats (choose per theme or force any treat, or keep drumsticks)
* Heart-shield armor icons
* Gradient experience bar with sparkles, matching level number and horse jump bar
* Pastel hotbar with a bow on the selected slot
* Pastel status-effect icons and frames
* Crosshair styles: heart, sparkle, flower, bow, dot, cross (or vanilla)

### Cosmetic effects (only you see them)
Ambient sparkles, cherry blossom petals outdoors, hearts when healing, flowers popping up under your
feet on grass and dirt, a sparkle burst on level-up, and pink enchanting-table glyphs. All of them
respect the vanilla *Particles* setting and have their own density slider.

### Audio
*Sugarplum Lullaby*, an original music-box waltz, replaces the vanilla menu music (it follows the
Music volume slider), plus soft chime sounds on Pinkified's own buttons.

### Settings
Open them from the heart button on the main menu or pause menu, from **Mods → Pinkified → Config**
(Forge and NeoForge), or with an optional key binding (*Options → Controls → Pinkified*, unbound by default).
Tabs: **Theme**, **Interface**, **HUD**, **Effects**, **Motion & Sound**. Every change previews live, and
the config is saved to `config/pinkified-client.toml` (which can also be edited by hand).

## Installation

1. Install Fabric (with Fabric API) or NeoForge (Forge on 1.20.1) for your Minecraft version.
2. Download the jar that matches your Minecraft version and loader (table above).
3. Put it in the `mods` folder of your game. Servers do not need it.

## Building

Each Minecraft version is a separate MultiLoader Gradle workspace with its own wrapper. These are the only supported
targets (the NightBeam convention): 8 jars per release, all with the same version.

| Minecraft | Loaders | Java | Folder |
| --- | --- | --- | --- |
| 1.20.1 | Forge, Fabric | 17 bytecode (build on 21) | `1.20.1/{common,forge,fabric}` |
| 1.21.1 | NeoForge, Fabric | 21 | `1.21.1/{common,neoforge,fabric}` |
| 26.2 | NeoForge, Fabric | 25 | `26.2/{common,neoforge,fabric}` |
| 26.3 | NeoForge, Fabric | 25 | `26.3/{common,neoforge,fabric}` |

```bash
./gradlew buildAll             # every workspace, then the 8 loader jars into releases/
cd 1.20.1 && ./gradlew build   # Gradle on Java 21 (Java 17 toolchain)
cd 1.21.1 && ./gradlew build   # Java 21
cd 26.3 && ./gradlew build     # Java 25 (the same for 26.2)
```

The root build hands the 26.x workspaces `JAVA_HOME_25_X64` (or `-Pjava25Home=<jdk>`) and the others
`JAVA_HOME_21_X64` (or `-Pjava21Home=<jdk>`); without them each workspace uses the current `JAVA_HOME`.
Jars land in `<workspace>/<loader>/build/libs/pinkified-<loader>-<minecraft>-<version>.jar`. Every workspace's
`gradle.properties` carries the same `version=` (`./gradlew checkVersions`; set it with
`./gradlew syncVersion -PmodVersion=x.y.z`). `python3 scripts/check_targets.py workspaces` checks the targets, the
shared version and the metadata ranges. Release notes for a version live in `release/<version>/README.md` (or
`patchnotes/<version>.md`).

Useful tasks in a loader project (for example `cd 1.20.1 && ./gradlew :forge:runClient`): `runClient`, `runServer`,
and on 1.20.1 `runSnapshots`. `runSnapshots` launches the game with a developer harness that visits the main menu in
every theme, each settings tab, the options and multiplayer screens, creates a throwaway world, opens the inventory,
crafting, furnace, anvil, enchanting, chest, creative, pause and game-over screens, saves screenshots to
`runs/snapshots/screenshots/pinkified_*.png` and quits. It is only active with `-Dpinkified.devshots=true`.
On 1.21.1 and 26.x, starting `runClient` with the environment variable `PINKIFIED_SELFTEST=1` runs a short launch
check instead: once the main menu is up it loads every mixin target, opens the settings screen, logs
`[pinkified] self-test passed` and quits. CI runs it for every 1.21.1 and 26.x loader.

## Releasing

CI (`.github/workflows/ci.yml`) builds all 8 jars on every push to `main` that touches the build, checks their
metadata and contents, boots a dedicated server with the jar installed (it must start cleanly, since the mod is
client-only) and runs the 1.20.1 screenshot harness. Publishing (to CurseForge only) is
`.github/workflows/publish.yml`, run by hand (`version`, `dry_run`) or by a `v*.*.*` tag; it never runs on a plain
push. It refuses a second release of the mod on the same day (Africa/Tunis), takes the jars from `releases/`, the
GitHub Release or a build from source, and fails unless exactly the 8 jars are found. It needs the secrets
`CURSEFORGE_TOKEN` (upload) and `CURSEFORGE_API_KEY` (Core API, for the same-day guard) and the variable
`CURSEFORGE_ID` (a real release stops before building when it is unset; a dry run only warns).

## How it works

Pinkified replaces no vanilla classes and adds no registry entries.

* **Themed textures at runtime** (`client/texture`). On every resource reload the original GUI textures
  are read from the active resource packs (so other packs are respected) and kept in memory.
  Themed copies are uploaded as dynamic textures under the vanilla texture ids (and, on 1.21.1 and 26.x, written
  over the GUI sprites in the stitched atlas), so vanilla and modded screens pick them up without code changes.
  Interface greys map one-to-one onto a six-colour theme ramp, which keeps every bevel, slot and border in place.
  HUD sprites (hearts, treats, armor, bubbles, XP bar, hotbar frame) are original pixel art painted into the exact
  sprite slots vanilla reads, so vanilla still does all HUD logic and HUD mods keep working. No Mojang texture is
  redistributed.
* **Status-effect icons** are softened in place inside the atlas after it is stitched.
* **Screens** are styled through the loader's screen events (Forge/NeoForge `ScreenEvent`s, Fabric API's
  `ScreenEvents`): the main menu is replaced (only when the plain vanilla `TitleScreen` opens), backdrops are
  drawn after the default background, list panels are added around selection lists, and container decorations are
  drawn over the container window. Where Fabric API has no event (title screen swap, container decorations, the
  game-over screen, tooltip colours, menu music) a few small mixins stand in; on 26.x the container and game-over
  hooks are shared mixins for both loaders, because NeoForge 26.x no longer has those events.
* **HUD**: two small overlays (coloured crosshair, themed level number), as Forge/NeoForge GUI layers or Fabric
  HUD elements. If another mod hides the vanilla crosshair or XP bar, Pinkified's versions stay hidden too
  (Forge/NeoForge).
* **Particles** are spawned client-side straight into the particle engine with sprites from the
  particle atlas; no particle types (or any other registry entries) are added. The enchant glyph
  provider is replaced with an identical one that only changes colour.
* **Music** is swapped in when the vanilla menu music starts (`PlaySoundEvent`, or a mixin on Fabric), so the
  vanilla music manager still schedules it.

### Project layout

```
<version>/common/src/main/java/com/sugarplumstudios/pinkified/
  PinkifiedMod.java              mod id and shared constants
  platform/                      PinkPlatform: what each loader provides (config dir, key binding, mod list)
  client/PinkifiedClient.java    shared client entry point the loaders call
  client/config/                 the TOML config (pinkified-client.toml, same layout on every loader)
  client/theme/                  ThemeId, Palette (all theme colours), FoodStyle, CrosshairStyle
  client/texture/                runtime recolour pipeline, HUD pixel art, dynamic textures
  client/gui/                    rendering kit, animated backdrop, screen hooks
  client/gui/screen/             main menu and settings screens
  client/gui/widget/             rounded animated button, toggle, slider, theme card, cycler
  client/hud/                    crosshair and level overlays
  client/particle/               cosmetic particles and spawner, pink enchant glyphs
  client/sound/                  menu music swap, UI sounds
  client/dev/                    screenshot harness (1.20.1, dev only)
<version>/forge|neoforge|fabric/ loader entry points, platform implementation and mixins
tools/assetgen/AssetGenerator.java   generates every static PNG (original pixel art)
tools/audiogen/AudioGenerator.java   synthesises the lullaby and UI sounds (WAV, then ffmpeg to OGG)
tools/preview/ThemePreview.java      renders the texture pipeline offline for design review
```

Regenerating assets (the art is the same in every workspace, so write it into each `common` project):

```bash
for v in 1.20.1 1.21.1 26.2 26.3; do java tools/assetgen/AssetGenerator.java $v/common/src/main/resources; done
```

```bash
java tools/audiogen/AudioGenerator.java build/audio
```

then encode with `ffmpeg -i build/audio/sugarplum_lullaby.wav -c:a libvorbis -q:a 4 <version>/common/src/main/resources/assets/pinkified/sounds/music/sugarplum_lullaby.ogg`
(and the same for `click` and `chime` into `sounds/ui/`).

## Compatibility notes

* Resource packs: Pinkified recolours whatever GUI textures the active packs provide; turn off the
  relevant toggles to see a pack's art unchanged.
* Mods that add buttons to the vanilla title screen: their buttons do not appear on the Pinkified main
  menu. Turn off *Pinkified main menu* to keep the vanilla title screen (it is still recoloured).
* HUD mods (AppleSkin and others) keep working because hearts and food are themed through textures.
* Demo mode always uses the vanilla title screen.
* Fabric: the main menu's Mods button only appears when Mod Menu is installed, and the version line shows the vanilla
  text (Forge and NeoForge show their own branding lines).

## Known limitations

* Only `minecraft:` interface textures are themed. Mods with their own GUI textures keep their art
  (they still get pastel backgrounds, tooltips and recoloured vanilla buttons).
* Some colours are hard-coded in vanilla code rather than textures and stay vanilla: container label
  text (dark grey), selection-list scrollbars, the boss bars and the loading screen.
* The main menu replaces only the plain vanilla `TitleScreen`; buttons other mods add to that screen do
  not appear on it (toggle *Pinkified main menu* off to keep them).
* HD resource packs: recolouring works at any size, but the custom HUD sprites are only painted when
  `icons.png`/`widgets.png` are a multiple of 256 px wide (1.20.1).
* 26.x: the themed game-over screen no longer shows the hover text of the death message, and the custom crosshair
  is also drawn while the debug screen is open.
* The mod is client-only. CI boots a real dedicated server with the jar installed (1.20.1 Forge and Fabric, 26.3
  Fabric and NeoForge) to check that a server which has it by mistake still starts cleanly
  (`scripts/server-boot-test.sh`, `scripts/server-smoke-test.sh`).

## Roadmap

* Theme other modded GUIs through an opt-in texture list in the config.
* More HUD shapes per theme (bow hearts, star armor), boss bar and loading-screen theming.
* Custom panoramas per theme and an optional pastel sky/fog shader in the world.
* More languages (only English is included).
* Additional menu music tracks and per-theme sound sets.

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md).

## Security

See [.github/SECURITY.md](.github/SECURITY.md).

## License

Apache License 2.0, Sugarplum Studios (see [LICENSE](LICENSE)). All textures, music and sounds are
original works generated by the tools in this repository.
