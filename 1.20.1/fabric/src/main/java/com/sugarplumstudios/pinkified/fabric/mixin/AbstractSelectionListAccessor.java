package com.sugarplumstudios.pinkified.fabric.mixin;

import net.minecraft.client.gui.components.AbstractSelectionList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** The list bounds Forge exposes as getLeft/getRight/getTop/getBottom. */
@Mixin(AbstractSelectionList.class)
public interface AbstractSelectionListAccessor {
    @Accessor("x0")
    int pinkified$x0();

    @Accessor("x1")
    int pinkified$x1();

    @Accessor("y0")
    int pinkified$y0();

    @Accessor("y1")
    int pinkified$y1();
}
