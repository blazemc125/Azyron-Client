package com.azyron.client.flavor.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

/**
 * Screen switching for this Minecraft version. The entry point moved between
 * releases (Minecraft#setScreen here, Minecraft.gui#setScreen on 26.2+), so
 * every flavor supplies its own and the shared screens call this.
 */
public final class Navigation {

	private Navigation() {
	}

	public static void open(Minecraft minecraft, Screen screen) {
		minecraft.setScreen(screen);
	}
}
