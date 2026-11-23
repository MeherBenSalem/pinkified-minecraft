# Pinkified Minecraft

**The Complete Pink Overhaul**, part of the Sugarplum Studios collection.

Pinkified turns Minecraft's interface into a soft pastel world (coquette pinks, strawberry milk,
lavender, cream, bows, hearts, petals and sparkles) without touching gameplay. It is a
**client-side-only** Forge mod: servers never need it, and you can join any server with it installed.

| | |
|---|---|
| Minecraft | Java Edition 1.20.1 |
| Loader | Forge 47.x (built against 47.4.28, works on any 47 build) |
| Java | 17 |
| Side | Client only (`clientSideOnly=true`, `displayTest=IGNORE_ALL_VERSION`) |

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
  Forge mod list, language/accessibility buttons and a heart button for Pinkified settings.
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
* **Tooltips** in theme colours (tooltips that other mods colour themselves are left alone).
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
Open them from the heart button on the main menu or pause menu, from **Mods → Pinkified → Config**,
or with an optional key binding (*Options → Controls → Pinkified*, unbound by default).
Tabs: **Theme**, **Interface**, **HUD**, **Effects**, **Motion & Sound**. Every change previews live, and
the config is saved to `config/pinkified-client.toml` (which can also be edited by hand).

## Building

Requirements: a JDK 17 or newer to run Gradle. The build uses a Java 17 toolchain and downloads one
automatically if needed.

```bash
./gradlew build
```

The mod jar is written to `build/libs/pinkified-forge-1.20.1-1.0.0.jar`. Drop it into the `mods` folder of a
Forge 1.20.1 installation.

Useful tasks:

```bash
./gradlew runClient
```

```bash
./gradlew runSnapshots
```

`runSnapshots` launches the game with a developer harness that visits the main menu in every theme,
each settings tab, the options and multiplayer screens, creates a throwaway world, opens the inventory,
crafting, furnace, anvil, enchanting, chest, creative, pause and game-over screens, saves screenshots to
`run/screenshots/pinkified_*.png` and quits. It is only active with `-Dpinkified.devshots=true`.

## How it works

Pinkified uses no mixins, no access transformers and replaces no vanilla classes.

* **Themed textures at runtime** (`client/texture`). On every resource reload the original GUI textures
  are read from the active resource packs (so other packs are respected) and kept in memory.
  Themed copies are uploaded as dynamic textures under the vanilla texture ids, so vanilla and
  modded screens pick them up without code changes. Interface greys map one-to-one onto a six-colour
  theme ramp, which keeps every bevel, slot and border in place. HUD sprites (hearts, treats, armor,
  bubbles, XP bar, hotbar frame) are original pixel art painted into the exact sprite slots vanilla
  reads, so vanilla still does all HUD logic and HUD mods keep working. No Mojang texture is
  redistributed.
* **Status-effect icons** are softened in place inside the mob-effect atlas after it is stitched.
* **Screens** are styled through Forge screen events: the main menu is replaced (only when the plain
  vanilla `TitleScreen` opens), backdrops are drawn on `BackgroundRendered`, list panels are added
  around selection lists, and container decorations are drawn on `ContainerScreenEvent`.
* **HUD**: two small Forge overlays (coloured crosshair, themed level number). If another mod hides
  the vanilla crosshair or XP bar, Pinkified's versions stay hidden too.
* **Particles** are spawned client-side straight into the particle engine with sprites from the
  particle atlas; no particle types (or any other registry entries) are added. The enchant glyph
  provider is replaced with an identical one that only changes colour.
* **Music** is swapped in via `PlaySoundEvent`, so the vanilla music manager still schedules it.

### Project layout

```
src/main/java/com/sugarplumstudios/pinkified/
  PinkifiedMod.java              entry point (client only)
  client/PinkifiedClient.java    event wiring, config registration
  client/config/                 ForgeConfigSpec (pinkified-client.toml)
  client/theme/                  ThemeId, Palette (all theme colours), FoodStyle, CrosshairStyle
  client/texture/                runtime recolour pipeline, HUD pixel art, dynamic textures
  client/gui/                    rendering kit, animated backdrop, screen hooks
  client/gui/screen/             main menu and settings screens
  client/gui/widget/             rounded animated button, toggle, slider, theme card, cycler
  client/hud/                    crosshair and level overlays
  client/particle/               cosmetic particles and spawner, pink enchant glyphs
  client/sound/                  menu music swap, UI sounds
  client/dev/                    screenshot harness (dev only)
tools/assetgen/AssetGenerator.java   generates every static PNG (original pixel art)
tools/audiogen/AudioGenerator.java   synthesises the lullaby and UI sounds (WAV, then ffmpeg to OGG)
tools/preview/ThemePreview.java      renders the texture pipeline offline for design review
```

Regenerating assets:

```bash
java tools/assetgen/AssetGenerator.java src/main/resources
```

```bash
java tools/audiogen/AudioGenerator.java build/audio
```

then encode with `ffmpeg -i build/audio/sugarplum_lullaby.wav -c:a libvorbis -q:a 4 src/main/resources/assets/pinkified/sounds/music/sugarplum_lullaby.ogg`
(and the same for `click` and `chime` into `sounds/ui/`).

## Compatibility notes

* Resource packs: Pinkified recolours whatever GUI textures the active packs provide; turn off the
  relevant toggles to see a pack's art unchanged.
* Mods that add buttons to the vanilla title screen: their buttons do not appear on the Pinkified main
  menu. Turn off *Pinkified main menu* to keep the vanilla title screen (it is still recoloured).
* HUD mods (AppleSkin and others) keep working because hearts and food are themed through textures.
* Demo mode always uses the vanilla title screen.

## Known limitations

* Only `minecraft:` interface textures are themed. Mods with their own GUI textures keep their art
  (they still get pastel backgrounds, tooltips and recoloured vanilla buttons).
* Some colours are hard-coded in vanilla code rather than textures and stay vanilla: container label
  text (dark grey), selection-list scrollbars, the boss bars and the loading screen.
* The main menu replaces only the plain vanilla `TitleScreen`; buttons other mods add to that screen do
  not appear on it (toggle *Pinkified main menu* off to keep them).
* HD resource packs: recolouring works at any size, but the custom HUD sprites are only painted when
  `icons.png`/`widgets.png` are a multiple of 256 px wide.
* The mod is client-only. CI boots a real Forge 1.20.1 dedicated server with the jar installed to check
  that a server which has it by mistake still starts cleanly (`scripts/server-boot-test.sh`).

## Roadmap

* Theme other modded GUIs through an opt-in texture list in the config.
* More HUD shapes per theme (bow hearts, star armor), boss bar and loading-screen theming.
* Custom panoramas per theme and an optional pastel sky/fog shader in the world.
* More languages (only English is included).
* Additional menu music tracks and per-theme sound sets.

## License

Apache License 2.0, Sugarplum Studios (see [LICENSE](LICENSE)). All textures, music and sounds are
original works generated by the tools in this repository.
