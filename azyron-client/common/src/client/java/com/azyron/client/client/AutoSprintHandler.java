package com.azyron.client.client;

import net.minecraft.client.Minecraft;

/**
 * Fabric-friendly auto sprint: whenever the player is holding forward and is
 * not using an item, enable sprinting. Never throws; a failure here must not
 * take the tick loop down with it.
 */
public final class AutoSprintHandler {

	private AutoSprintHandler() {
	}

	public static void onClientTick(Minecraft minecraft) {
		AzyronClientState state = AzyronClientState.get();
		if (!state.config().autoSprint) {
			return;
		}
		try {
			if (minecraft.player == null || minecraft.options == null) {
				return;
			}
			if (!minecraft.options.keyUp.isDown()) {
				return;
			}
			if (minecraft.player.isUsingItem()) {
				return;
			}
			if (!minecraft.player.isSprinting()) {
				minecraft.player.setSprinting(true);
			}
		} catch (Exception ignored) {
			// Auto sprint is best effort only.
		}
	}
}
