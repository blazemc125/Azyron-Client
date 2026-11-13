package com.azyron.client.client;

/**
 * Graphics abstraction over the two GUI APIs in play:
 * <ul>
 *   <li>Minecraft 1.21.11: {@code net.minecraft.client.gui.GuiGraphics}</li>
 *   <li>Minecraft 26.x: {@code net.minecraft.client.gui.GuiGraphicsExtractor}</li>
 * </ul>
 * Each version flavor ships a small adapter implementing this interface, which
 * lets every HUD element and screen live in the shared source set.
 */
public interface AzyronUi {

	int width();

	int height();

	void fill(int x1, int y1, int x2, int y2, int argb);

	void text(String text, int x, int y, int argb);

	void centeredText(String text, int centerX, int y, int argb);

	int textWidth(String text);

	/** Scale subsequent drawing around a pivot, until {@link #popScale()}. */
	void pushScale(float scale, float pivotX, float pivotY);

	void popScale();
}
