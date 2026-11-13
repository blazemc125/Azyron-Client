package com.azyron.client.client.mixin;

import com.azyron.client.client.screens.AzyronHomeScreen;
import com.azyron.client.flavor.ui.Navigation;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Adds the compact AZYRON entry button to the vanilla title screen.
 * Deliberately minimal: the vanilla button grid stays untouched.
 */
@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {

	protected TitleScreenMixin() {
		super(Component.empty());
	}

	@Inject(method = "init()V", at = @At("TAIL"))
	private void azyron$addEntryButton(CallbackInfo ci) {
		this.addRenderableWidget(Button.builder(Component.literal("AZYRON V3"),
					button -> Navigation.open(this.minecraft, new AzyronHomeScreen()))
				.pos(8, 8)
				.size(96, 20)
				.build());
	}
}
