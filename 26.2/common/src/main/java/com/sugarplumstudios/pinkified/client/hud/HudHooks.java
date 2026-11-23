package com.sugarplumstudios.pinkified.client.hud;

import net.minecraft.client.renderer.RenderPipelines;
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
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

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

    /** Remembers whether another mod hid the vanilla crosshair, so ours stays hidden too. */
    public static void setCrosshairCancelled(boolean cancelled) {
        crosshairCancelled = cancelled;
    }

    /** Remembers whether another mod hid the vanilla experience bar, so our level number stays hidden too. */
    public static void setXpCancelled(boolean cancelled) {
        xpCancelled = cancelled;
    }

    /** Drawn right above the vanilla crosshair. */
    public static void renderCrosshair(GuiGraphicsExtractor g, int width, int height) {
        CrosshairStyle style = PinkConfig.get(PinkConfig.CROSSHAIR);
        if (style == CrosshairStyle.VANILLA || crosshairCancelled) return;
        Minecraft mc = Minecraft.getInstance();
        Options o = mc.options;
        if (mc.gui.hud.isHidden() || mc.player == null || mc.gameMode == null || !o.getCameraType().isFirstPerson()) return;
        if (mc.gameMode.getPlayerMode() == GameType.SPECTATOR && !canSpectatorTarget(mc, mc.hitResult)) return;
        int c = ThemedTextures.CROSSHAIR_CELL;
        g.blit(RenderPipelines.GUI_TEXTURED, ThemedTextures.CROSSHAIRS, (width - c) / 2, (height - c) / 2, style.ordinal() * c, 0, c, c,
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

    /** Drawn right above the vanilla experience bar and its level number. */
    public static void renderXpLevel(GuiGraphicsExtractor g, int width, int height) {
        if (!PinkConfig.on(PinkConfig.THEME_XP) || xpCancelled) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.gameMode == null || mc.gui.hud.isHidden()) return;
        if (mc.player.jumpableVehicle() != null || !mc.gameMode.hasExperience()) return;
        int level = mc.player.experienceLevel;
        if (level <= 0) return;
        // drawn exactly over vanilla's green number, covering it completely
        Font font = mc.font;
        String s = Integer.toString(level);
        int x = (width - font.width(s)) / 2;
        int y = height - 31 - 4;
        Palette p = Themes.palette();
        RenderKit.outlinedText(g, font, s, x, y, Colors.lerp(p.heart, p.light, 0.3f), p.heartInk);
    }
}
