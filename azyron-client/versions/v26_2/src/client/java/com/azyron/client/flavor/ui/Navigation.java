package com.azyron.client.flavor.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

/**
 * Screen switching for this Minecraft version. From 26.2 the entry point is
 * Minecraft.gui#setScreen; the shared screens call this instead of the
 * Minecraft method directly.
 */
public final class Navigation {

	private Navigation() {
	}

	public static void open(Minecraft minecraft, Screen screen) {
		minecraft.gui.setScreen(screen);
	}
}
