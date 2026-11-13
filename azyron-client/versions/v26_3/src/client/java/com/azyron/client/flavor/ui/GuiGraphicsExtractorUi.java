package com.azyron.client.flavor.ui;

import com.azyron.client.client.AzyronUi;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * AzyronUi adapter for Minecraft 26.x (GuiGraphicsExtractor).
 */
public final class GuiGraphicsExtractorUi implements AzyronUi {

	private final GuiGraphicsExtractor graphics;
	private final Minecraft minecraft = Minecraft.getInstance();

	public GuiGraphicsExtractorUi(GuiGraphicsExtractor graphics) {
		this.graphics = graphics;
	}

	@Override
	public int width() {
		return graphics.guiWidth();
	}

	@Override
	public int height() {
		return graphics.guiHeight();
	}

	@Override
	public void fill(int x1, int y1, int x2, int y2, int argb) {
		graphics.fill(x1, y1, x2, y2, argb);
	}

	@Override
	public void text(String text, int x, int y, int argb) {
		graphics.text(minecraft.font, text, x, y, argb);
	}

	@Override
	public void centeredText(String text, int centerX, int y, int argb) {
		graphics.centeredText(minecraft.font, text, centerX, y, argb);
	}

	@Override
	public int textWidth(String text) {
		return minecraft.font.width(text);
	}

	@Override
	public void pushScale(float scale, float pivotX, float pivotY) {
		graphics.pose().pushMatrix();
		graphics.pose().translate(pivotX, pivotY);
		graphics.pose().scale(scale);
		graphics.pose().translate(-pivotX, -pivotY);
	}

	@Override
	public void popScale() {
		graphics.pose().popMatrix();
	}
}
