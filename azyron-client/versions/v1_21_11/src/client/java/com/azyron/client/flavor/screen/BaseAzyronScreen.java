package com.azyron.client.flavor.screen;

import com.azyron.client.client.AzyronUi;
import com.azyron.client.flavor.ui.GuiGraphicsUi;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Base class bridging the 1.21.11 Screen API (GuiGraphics based)
 * onto the shared AzyronUi abstraction.
 */
public abstract class BaseAzyronScreen extends Screen {

	protected BaseAzyronScreen() {
		super(Component.literal("AZYRON CLIENT V3"));
	}

	protected abstract void paint(AzyronUi ui, int mouseX, int mouseY, float delta);

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		this.renderBackground(graphics, mouseX, mouseY, delta);
		paint(new GuiGraphicsUi(graphics), mouseX, mouseY, delta);
		super.render(graphics, mouseX, mouseY, delta);
	}
}
