# Marketing assets: 1.0

Listing copy and artwork for CurseForge (NightBeam publishes to CurseForge only). Nothing here ships in the mod JAR.

## Listing

| File | Use |
| --- | --- |
| [`curseforge/description.md`](curseforge/description.md) | Full CurseForge project description (Markdown) |
| [`curseforge/summary.txt`](curseforge/summary.txt) | Short CurseForge project summary (214 characters) |
| `images/icon.png` | Project avatar |

Image links in the description point at `raw.githubusercontent.com/MeherBenSalem/pinkified-minecraft/main/marketing/images/...`. **They only work for visitors once this folder is on `main` and the repository is public.** While the repository is private, upload the images to CurseForge instead (the description editor takes uploads) and replace each URL.

The description leads with the pitch, then the themes, the main menu, inventories and screens, the HUD, cosmetic effects, music and sound, gentle touches, settings, configuration, how it works, downloads and compatibility (with the known limitations), the FAQ and a gallery.

### Gallery screenshots

The CurseForge gallery shows these screenshots, captioned with the alt texts in `curseforge/description.md`:

| Screenshot | Caption |
| --- | --- |
| `title.jpg` | The Pinkified main menu in Strawberry Milk |
| `title-lavender.jpg` | The main menu in Lavender Dreams |
| `title-coquette.jpg` | The main menu in Pink Coquette |
| `title-cherry.jpg` | The main menu in Cherry Blossom |
| `title-angelcore.jpg` | The main menu in Angelcore |
| `title-princess.jpg` | The main menu in Pastel Princess |
| `title-custom.jpg` | The main menu in a Custom theme |
| `title-vanilla.jpg` | The vanilla title screen, recoloured, with the Pinkified main menu turned off |
| `hud.jpg` | The HUD in Strawberry Milk: hearts, strawberries, armor, the XP bar and the hotbar |
| `hud-lavender.jpg` | The HUD in Lavender Dreams |
| `hud-angelcore.jpg` | The HUD in Angelcore |
| `third-person.jpg` | Petals, sparkles and a heart around the player outdoors |
| `inventory.jpg` | The inventory in Strawberry Milk |
| `inventory-lavender.jpg` | The inventory in Lavender Dreams |
| `creative.jpg` | The creative inventory with pastel status-effect icons |
| `crafting.jpg` | The crafting table |
| `furnace.jpg` | The furnace |
| `furnace-angelcore.jpg` | The furnace in Angelcore |
| `chest.jpg` | A chest with its little bow |
| `anvil.jpg` | The anvil |
| `enchanting.jpg` | The enchanting table |
| `pause.jpg` | The pause menu with its heart button |
| `death.jpg` | The pastel game-over screen |
| `options.jpg` | The Options screen on the pastel sky |
| `video.jpg` | Video Settings on a rounded list panel |
| `multiplayer.jpg` | The multiplayer screen with the sky instead of dirt |
| `settings-interface.jpg` | Pinkified Settings: the Interface tab |
| `settings-hud.jpg` | Pinkified Settings: the HUD tab with its live preview |
| `settings-effects.jpg` | Pinkified Settings: the Effects tab |
| `settings-motion.jpg` | Pinkified Settings: the Motion & Sound tab |

`settings-theme.jpg` is left out of the gallery and the cards: in the 1.0.0 build the Theme tab cuts off the longer theme names on its theme cards ("Strawberry Mil", "Cherry Blossor"). Add it back once that is fixed and the screenshot retaken.

## Adding a Minecraft version

The supported versions are listed in these places:

1. `VERSIONS` at the top of [`source/render.cjs`](source/render.cjs). Add one line, for example `{ minecraft: '1.21.1', loaders: ['NeoForge'] },`. The banner's loader pill and version line are built from it. Then rerun the script for `banner`.
2. The table under **Downloads & Compatibility** in `curseforge/description.md`. Add one row per loader.
3. The FAQ answer **Is there a Fabric or NeoForge version?** in `curseforge/description.md`.

No other copy or artwork names a Minecraft version. A new release number goes in `RELEASE` in `render.cjs` and in the banner's alt text on the first line of the description.

## Images

| File | Size | Use |
| --- | --- | --- |
| `images/banner.png` | 1800×600, 502 KB | Description header: the title, "The Complete Pink Overhaul.", the Fabric, Forge and NeoForge pills and the client-side pill, the supported Minecraft versions, and the Strawberry Milk main menu |
| `images/icon.png` | 512×512, 128 KB | Project avatar: a Strawberry Milk heart in a candy-striped ring |
| `images/card-themes.png` | 1600×900, 670 KB | The six themes and Custom, each with its main menu, heart, treat, armor and colours |
| `images/card-menu.png` | 1600×900, 731 KB | The main menu in Strawberry Milk, plus four more themes |
| `images/card-screens.png` | 1600×900, 685 KB | The inventory in Strawberry Milk and the furnace in Angelcore |
| `images/card-hud.png` | 1600×900, 749 KB | The in-game HUD, the heart states, armor, air, the six treats and the six crosshairs |
| `images/card-effects.png` | 1600×900, 1123 KB | Particles around the player outdoors, plus the six particle sprites |
| `images/card-touches.png` | 1600×900, 742 KB | The pause menu, the game-over screen and the sky behind the multiplayer screen |
| `images/card-settings.png` | 1600×900, 605 KB | Pinkified Settings: the Effects and HUD tabs |
| `images/card-config.png` | 1600×900, 546 KB | An excerpt of `pinkified-client.toml` with its defaults |
| `images/card-clientside.png` | 1600×900, 1062 KB | The recoloured vanilla title screen, with client-side facts |
| `images/header-*.png` | 1400×120, transparent, 30–54 KB | 13 section headings for the description |
| `images/divider.png` | 1400×40, transparent | Section divider |
| `images/screenshots/*.jpg` | 1920×1080, 78–432 KB | 31 captures: gallery images and card art |

### Where the pictures come from

- **Screenshots** are unedited in-game captures, converted to JPEG. They were taken in GitHub Actions by the mod's own screenshot harness (`cd 1.20.1 && ./gradlew :forge:runSnapshots`, `1.20.1/common/.../client/dev/DevSnapshots.java`; they were taken on Forge 1.20.1 and look the same on every loader and version) at 1920×1080 under a virtual display: it visits the main menu in every theme, each settings tab, the options and multiplayer screens, then creates a throwaway world and opens the inventory, crafting, furnace, anvil, enchanting, chest, creative, pause and game-over screens. The 1920×1080 window size is set on the `snapshots` run in `1.20.1/forge/build.gradle` (and `1.20.1/fabric/build.gradle`).
- **HUD sprites** on the cards, banner and icon (hearts, treats, armor, bubbles, crosshairs) are painted without Minecraft by [`source/HudRenders.java`](source/HudRenders.java), which calls the mod's own `HudSprites` pixel art for each theme. They are in `source/renders/`, 24 PNGs per theme.
- **Particle sprites** are the mod's own textures from `1.20.1/common/src/main/resources/assets/pinkified/textures/particle/` (the same in every workspace).

## Regenerating the artwork

Repaint the HUD sprites (from the repository root):

```bash
S=1.20.1/common/src/main/java/com/sugarplumstudios/pinkified/client
javac -d build/hudrenders $S/util/Colors.java $S/theme/Palette.java $S/theme/ThemeId.java $S/theme/FoodStyle.java \
  $S/theme/CrosshairStyle.java $S/texture/Canvas.java $S/texture/HudSprites.java $S/texture/Recolor.java \
  $S/texture/Patterns.java marketing/source/HudRenders.java
java -cp build/hudrenders HudRenders marketing/source/renders
```

The cards, banner, icon and headers are HTML rendered with Playwright from the screenshots, the HUD sprites and the bundled fonts:

```bash
NODE_PATH="$(npm root -g)" node marketing/source/render.cjs            # everything
NODE_PATH="$(npm root -g)" node marketing/source/render.cjs banner icon  # selected assets
```

- Set `CHROMIUM=/path/to/chromium` to render with a Chromium other than Playwright's own download.
- `source/logo.png` is the mod-list logo (`1.20.1/common/src/main/resources/pinkified_logo.png`).
- `source/fonts/` holds Fredoka and Nunito with their SIL Open Font License 1.1 texts. The script uses only these files.

Theme names, colours and treats on the cards are written at the top of `render.cjs`, copied from `theme/Palette.java` and the lang file. Update them when those change.

## Wording

Every claim in the copy and on the cards comes from the 1.0.0 source: the themes and treats from `theme/ThemeId.java`, `theme/FoodStyle.java` and the lang file, the crosshairs from `theme/CrosshairStyle.java`, and the 34 settings and their defaults from `config/PinkConfig.java`. Update the copy when those change. Call the six presets and Custom **themes**, the food icons **treats**, the settings screen **Pinkified Settings** and the menu track **Sugarplum Lullaby**. Don't promise anything about specific servers or mods beyond what the project README says.
