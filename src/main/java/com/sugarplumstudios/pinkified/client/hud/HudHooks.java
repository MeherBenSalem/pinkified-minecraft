package com.sugarplumstudios.pinkified.client.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import com.sugarplumstudios.pinkified.client.config.PinkConfig;
import com.sugarplumstudios.pinkified.client.gui.RenderKit;
import com.sugarplumstudios.pinkified.client.texture.ThemedTextures;
import com.sugarplumstudios.pinkified.client.theme.CrosshairStyle;
import com.sugarplumstudios.pinkified.client.theme.Palette;
import com.sugarplumstudios.pinkified.client.theme.Themes;
import com.sugarplumstudios.pinkified.client.util.Colors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * HUD additions. Hearts, food, armor and bars are themed purely through textures (vanilla keeps
 * drawing them, so HUD mods such as AppleSkin keep working). Only two small overlays are added:
 * the coloured crosshair and the themed experience level number.
 */
public final class HudHooks {
    private static boolean crosshairCancelled;
    private static boolean xpCancelled;

    private HudHooks() {
    }

    public static void registerOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAbove(VanillaGuiOverlay.CROSSHAIR.id(), "crosshair", HudHooks::renderCrosshair);
        event.registerAbove(VanillaGuiOverlay.EXPERIENCE_BAR.id(), "experience_level", HudHooks::renderXpLevel);
    }

    /** Remembers whether another mod hid the vanilla element, so ours stays hidden too. */
    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public static void onOverlayPre(RenderGuiOverlayEvent.Pre event) {
        if (event.getOverlay().id().equals(VanillaGuiOverlay.CROSSHAIR.id())) crosshairCancelled = event.isCanceled();
        else if (event.getOverlay().id().equals(VanillaGuiOverlay.EXPERIENCE_BAR.id())) xpCancelled = event.isCanceled();
    }

    private static void renderCrosshair(ForgeGui gui, GuiGraphics g, float partialTick, int width, int height) {
        CrosshairStyle style = PinkConfig.get(PinkConfig.CROSSHAIR);
        if (style == CrosshairStyle.VANILLA || crosshairCancelled) return;
        Minecraft mc = gui.getMinecraft();
        Options o = mc.options;
        if (o.hideGui || mc.player == null || mc.gameMode == null || !o.getCameraType().isFirstPerson()) return;
        if (mc.gameMode.getPlayerMode() == GameType.SPECTATOR && !canSpectatorTarget(mc, mc.hitResult)) return;
        if (o.renderDebug && !mc.player.isReducedDebugInfo() && !o.reducedDebugInfo().get()) return;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        int c = ThemedTextures.CROSSHAIR_CELL;
        g.blit(ThemedTextures.CROSSHAIRS, (width - c) / 2, (height - c) / 2, style.ordinal() * c, 0, c, c,
                c * CrosshairStyle.values().length, c);
    }

    private static boolean canSpectatorTarget(Minecraft mc, HitResult hit) {
        if (hit == null || mc.level == null) return false;
        if (hit.getType() == HitResult.Type.ENTITY) return ((EntityHitResult) hit).getEntity() instanceof MenuProvider;
        if (hit.getType() == HitResult.Type.BLOCK) {
            BlockPos pos = ((BlockHitResult) hit).getBlockPos();
            return mc.level.getBlockState(pos).getMenuProvider(mc.level, pos) != null;
        }
        return false;
    }

    private static void renderXpLevel(ForgeGui gui, GuiGraphics g, float partialTick, int width, int height) {
        if (!PinkConfig.on(PinkConfig.THEME_XP) || xpCancelled) return;
        Minecraft mc = gui.getMinecraft();
        if (mc.player == null || mc.gameMode == null || mc.options.hideGui) return;
        if (mc.player.jumpableVehicle() != null || !mc.gameMode.hasExperience()) return;
        int level = mc.player.experienceLevel;
        if (level <= 0) return;
        // drawn exactly over vanilla's green number, covering it completely
        Font font = gui.getFont();
        String s = Integer.toString(level);
        int x = (width - font.width(s)) / 2;
        int y = height - 31 - 4;
        Palette p = Themes.palette();
        RenderKit.outlinedText(g, font, s, x, y, Colors.lerp(p.heart, p.light, 0.3f), p.heartInk);
    }
}
