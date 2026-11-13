package com.azyron.client.flavor.screen;

import com.azyron.client.client.AzyronUi;
import com.azyron.client.flavor.ui.GuiGraphicsExtractorUi;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Base class bridging the 26.x Screen API (GuiGraphicsExtractor based)
 * onto the shared AzyronUi abstraction.
 */
public abstract class BaseAzyronScreen extends Screen {

	protected BaseAzyronScreen() {
		super(Component.literal("AZYRON CLIENT V3"));
	}

	protected abstract void paint(AzyronUi ui, int mouseX, int mouseY, float delta);

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		this.extractBackground(graphics, mouseX, mouseY, delta);
		paint(new GuiGraphicsExtractorUi(graphics), mouseX, mouseY, delta);
		super.extractRenderState(graphics, mouseX, mouseY, delta);
	}
}
