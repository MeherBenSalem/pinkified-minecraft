package com.sugarplumstudios.pinkified.client.texture;

import java.util.Map;

/**
 * A plain ARGB pixel buffer. Texture generation works on these instead of NativeImage so the
 * same code runs inside the game and in offline preview tools.
 *
 * <p>Drawing calls take coordinates in "template pixels"; {@link #scale} expands each one into
 * a scale x scale block, which keeps generated art crisp inside HD resource packs.
 */
public final class Canvas {
    public final int width, height;
    public final int[] px;
    public int scale = 1;

    public Canvas(int width, int height) {
        this(width, height, new int[width * height]);
    }

    public Canvas(int width, int height, int[] px) {
        this.width = width;
        this.height = height;
        this.px = px;
    }

    public Canvas copy() {
        Canvas c = new Canvas(width, height, px.clone());
        c.scale = scale;
        return c;
    }

    public int get(int x, int y) {
        return x >= 0 && y >= 0 && x < width && y < height ? px[y * width + x] : 0;
    }

    /** Sets one raw pixel, ignoring {@link #scale}. */
    public void setRaw(int x, int y, int argb) {
        if (x >= 0 && y >= 0 && x < width && y < height) px[y * width + x] = argb;
    }

    /** Sets one template pixel (a scale x scale block). */
    public void set(int x, int y, int argb) {
        for (int dy = 0; dy < scale; dy++)
            for (int dx = 0; dx < scale; dx++) setRaw(x * scale + dx, y * scale + dy, argb);
    }

    /** Clears a rectangle given in template pixels. */
    public void clear(int x, int y, int w, int h) {
        fill(x, y, w, h, 0);
    }

    public void fill(int x, int y, int w, int h, int argb) {
        for (int yy = y; yy < y + h; yy++)
            for (int xx = x; xx < x + w; xx++) set(xx, yy, argb);
    }

    /**
     * Paints a character template at (ox, oy). Characters missing from the palette, and '.',
     * are left untouched.
     */
    public void stamp(String[] rows, int ox, int oy, Map<Character, Integer> palette) {
        stamp(rows, ox, oy, palette, 0, Integer.MAX_VALUE);
    }

    /** Like {@link #stamp} but only columns in [minCol, maxCol] are painted. */
    public void stamp(String[] rows, int ox, int oy, Map<Character, Integer> palette, int minCol, int maxCol) {
        for (int y = 0; y < rows.length; y++) {
            String row = rows[y];
            for (int x = Math.max(0, minCol); x < row.length() && x <= maxCol; x++) {
                Integer c = palette.get(row.charAt(x));
                if (c != null) set(ox + x, oy + y, c);
            }
        }
    }

    /** Copies a rectangle (template pixels) from another canvas of the same size. */
    public void copyFrom(Canvas src, int x, int y, int w, int h) {
        int s = scale;
        for (int yy = y * s; yy < (y + h) * s; yy++)
            for (int xx = x * s; xx < (x + w) * s; xx++) setRaw(xx, yy, src.get(xx, yy));
    }
}
