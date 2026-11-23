package com.sugarplumstudios.pinkified.client.dev;

import com.mojang.logging.LogUtils;
import com.sugarplumstudios.pinkified.client.PinkifiedClient;
import com.sugarplumstudios.pinkified.client.config.PinkConfig;
import com.sugarplumstudios.pinkified.client.gui.screen.PinkSettingsScreen;
import com.sugarplumstudios.pinkified.client.theme.ThemeId;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.VideoSettingsScreen;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.gui.screens.inventory.CraftingScreen;
import net.minecraft.client.gui.screens.inventory.EnchantmentScreen;
import net.minecraft.client.gui.screens.inventory.FurnaceScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.tutorial.TutorialSteps;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.inventory.FurnaceMenu;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

/**
 * Developer tool for visual regression checks. Only active when the game is started with
 * {@code -Dpinkified.devshots=true} (see the {@code snapshots} run in build.gradle): it visits the
 * main menus, creates a throwaway world, opens each themed screen, saves screenshots into
 * {@code run/screenshots/} and quits. It does nothing in normal play.
 */
public final class DevSnapshots {
    private static final Logger LOGGER = LogUtils.getLogger();

    private record Step(int delay, BooleanSupplier ready, Runnable action) {
    }

    private static final List<Step> steps = new ArrayList<>();
    private static int index;
    private static int wait;

    private DevSnapshots() {
    }

    public static boolean enabled() {
        return Boolean.getBoolean("pinkified.devshots");
    }

    private static Minecraft mc() {
        return Minecraft.getInstance();
    }

    private static void step(int delay, Runnable action) {
        steps.add(new Step(delay, () -> true, action));
    }

    private static void waitFor(BooleanSupplier ready, Runnable action) {
        steps.add(new Step(20, ready, action));
    }

    private static void shot(String name) {
        step(30, () -> Screenshot.grab(mc().gameDirectory, "pinkified_" + name + ".png", mc().getMainRenderTarget(),
                msg -> LOGGER.info("[devshots] {}", msg.getString())));
    }

    private static void theme(ThemeId id) {
        PinkConfig.set(PinkConfig.THEME, id);
        PinkifiedClient.applySettings();
    }

    private static void command(String cmd) {
        if (mc().player != null) mc().player.connection.sendCommand(cmd);
    }

    /** Clears chat and the movement tutorial toast so the HUD is unobstructed. */
    private static void quietHud() {
        mc().gui.getChat().clearMessages(false);
        mc().getTutorial().setStep(TutorialSteps.NONE);
        mc().getToasts().clear();
    }

    private static Inventory inv() {
        return mc().player.getInventory();
    }

    static {
        waitFor(() -> mc().getOverlay() == null && mc().screen != null && mc().level == null, () -> {
            mc().options.onboardAccessibility = false;
            mc().setScreen(new TitleScreen());
        });
        for (ThemeId id : ThemeId.values()) {
            step(10, () -> theme(id));
            shot("title_" + id.id());
        }
        step(5, () -> theme(ThemeId.STRAWBERRY_MILK));
        step(5, () -> {
            PinkConfig.set(PinkConfig.CUSTOM_MAIN_MENU, false);
            mc().setScreen(new TitleScreen());
        });
        shot("title_vanilla_recoloured");
        step(5, () -> PinkConfig.set(PinkConfig.CUSTOM_MAIN_MENU, true));
        for (int tab = 0; tab < 5; tab++) {
            int t = tab;
            step(5, () -> mc().setScreen(PinkSettingsScreen.atTab(new TitleScreen(), t)));
            shot("settings_" + t);
        }
        step(5, () -> mc().setScreen(new OptionsScreen(new TitleScreen(), mc().options)));
        shot("options");
        step(5, () -> mc().setScreen(new VideoSettingsScreen(mc().screen, mc().options)));
        shot("video");
        step(5, () -> mc().setScreen(new JoinMultiplayerScreen(new TitleScreen())));
        shot("multiplayer");
        step(5, () -> {
            LevelSettings settings = new LevelSettings("Pinkified Snapshots", GameType.SURVIVAL, false, Difficulty.EASY, true,
                    new GameRules(), WorldDataConfiguration.DEFAULT);
            mc().createWorldOpenFlows().createFreshLevel("pinkified_snapshots", settings, new WorldOptions(20231103L, false, false),
                    WorldPresets::createNormalWorldDimensions);
        });
        waitFor(() -> mc().level != null && mc().player != null && mc().screen == null, () -> {
            command("gamerule doMobSpawning false");
            command("gamerule doDaylightCycle false");
            command("time set 1000");
            command("weather clear");
            command("xp add @s 7 levels");
            command("xp add @s 40 points");
            command("item replace entity @s armor.chest with minecraft:diamond_chestplate{Enchantments:[{id:\"minecraft:protection\",lvl:1}]}");
            command("item replace entity @s armor.head with minecraft:iron_helmet");
            command("item replace entity @s hotbar.0 with minecraft:diamond_sword{Enchantments:[{id:\"minecraft:sharpness\",lvl:1}]}");
            command("item replace entity @s hotbar.1 with minecraft:cherry_sapling 16");
            command("item replace entity @s hotbar.2 with minecraft:cake");
            command("item replace entity @s hotbar.3 with minecraft:pink_wool 64");
            command("effect give @s minecraft:speed 9999 0 true");
            command("effect give @s minecraft:night_vision 9999 0 true");
        });
        step(120, () -> command("damage @s 9"));
        step(40, () -> command("effect give @s minecraft:regeneration 30 1"));
        step(30, DevSnapshots::quietHud);
        shot("hud");
        step(5, () -> mc().options.setCameraType(CameraType.THIRD_PERSON_BACK));
        shot("third_person");
        step(5, () -> mc().options.setCameraType(CameraType.FIRST_PERSON));
        step(5, () -> mc().setScreen(new InventoryScreen(mc().player)));
        shot("inventory");
        step(5, () -> mc().setScreen(new CraftingScreen(new CraftingMenu(101, inv()), inv(), Component.translatable("container.crafting"))));
        shot("crafting");
        step(5, () -> mc().setScreen(new FurnaceScreen(new FurnaceMenu(102, inv()), inv(), Component.translatable("container.furnace"))));
        shot("furnace");
        step(5, () -> mc().setScreen(new AnvilScreen(new AnvilMenu(103, inv()), inv(), Component.translatable("container.repair"))));
        shot("anvil");
        step(5, () -> mc().setScreen(new EnchantmentScreen(new EnchantmentMenu(104, inv()), inv(), Component.translatable("container.enchant"))));
        shot("enchanting");
        step(5, () -> mc().setScreen(new ContainerScreen(ChestMenu.threeRows(105, inv()), inv(), Component.translatable("container.chest"))));
        shot("chest");
        step(5, () -> mc().setScreen(new PauseScreen(true)));
        shot("pause");
        step(5, () -> mc().setScreen(new DeathScreen(Component.literal("Dev was hugged too tightly by a teddy bear"), false)));
        shot("death");
        step(5, () -> {
            mc().setScreen(null);
            command("gamemode creative");
        });
        step(20, () -> mc().setScreen(new InventoryScreen(mc().player)));
        shot("creative");
        step(5, () -> {
            mc().setScreen(null);
            command("gamemode survival");
            theme(ThemeId.LAVENDER_DREAMS);
        });
        step(5, DevSnapshots::quietHud);
        shot("hud_lavender");
        step(5, () -> mc().setScreen(new InventoryScreen(mc().player)));
        shot("inventory_lavender");
        step(5, () -> {
            mc().setScreen(null);
            theme(ThemeId.ANGELCORE);
        });
        step(5, DevSnapshots::quietHud);
        shot("hud_angelcore");
        step(5, () -> mc().setScreen(new FurnaceScreen(new FurnaceMenu(106, inv()), inv(), Component.translatable("container.furnace"))));
        shot("furnace_angelcore");
        step(5, () -> {
            theme(ThemeId.STRAWBERRY_MILK);
            PinkConfig.save();
            LOGGER.info("[devshots] finished, quitting");
            mc().stop();
        });
    }

    public static void onClientTick() {
        if (index >= steps.size()) return;
        Step s = steps.get(index);
        if (!s.ready().getAsBoolean()) return;
        if (wait++ < s.delay()) return;
        wait = 0;
        index++;
        try {
            s.action().run();
        } catch (RuntimeException e) {
            LOGGER.error("[devshots] step {} failed", index - 1, e);
        }
    }
}
