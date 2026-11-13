package com.azyron.client.flavor;

import com.azyron.client.client.AzyronClientState;
import com.azyron.client.client.AzyronSharedInit;
import com.azyron.client.client.hud.AzyronHudRenderer;
import com.azyron.client.common.AzyronConstants;
import com.azyron.client.flavor.ui.GuiGraphicsUi;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.resources.Identifier;

public final class AzyronClientEntrypoint implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		AzyronSharedInit.init();
		HudElementRegistry.addLast(
				Identifier.fromNamespaceAndPath(AzyronConstants.MOD_ID, "hud"),
				(graphics, tickCounter) -> AzyronHudRenderer.render(new GuiGraphicsUi(graphics), AzyronClientState.get())
		);
	}
}
