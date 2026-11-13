package com.azyron.client.client.screens;

import com.azyron.client.client.AzyronClientState;
import com.azyron.client.client.AzyronUi;
import com.azyron.client.common.AzyronConstants;
import com.azyron.client.common.FpsBoostProfile;
import com.azyron.client.flavor.screen.BaseAzyronScreen;
import com.azyron.client.flavor.ui.Navigation;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;


/**
 * Client-wide settings: auto sprint, first-launch optimization and an
 * about panel with full credit and modpack disclaimer.
 */
public final class ClientSettingsScreen extends BaseAzyronScreen {

	private Button autoSprintButton;
	private Button autoApplyButton;
	private Button backButton;
	private int panelTop;

	@Override
	protected void init() {
		this.panelTop = 40;

		int controlsX = 18;
		int rowY = this.panelTop + 18;

		this.autoSprintButton = this.addRenderableWidget(Button.builder(
				Component.literal(autoSprintLabel()),
				button -> {
					AzyronClientState state = AzyronClientState.get();
					state.config().autoSprint = !state.config().autoSprint;
					state.saveConfig();
					button.setMessage(Component.literal(autoSprintLabel()));
				})
				.pos(controlsX, rowY)
				.size(190, 17)
				.build());

		this.autoApplyButton = this.addRenderableWidget(Button.builder(
				Component.literal(autoApplyLabel()),
				button -> {
					AzyronClientState state = AzyronClientState.get();
					state.config().autoApplyOptimization = !state.config().autoApplyOptimization;
					state.saveConfig();
					button.setMessage(Component.literal(autoApplyLabel()));
				})
				.pos(controlsX, rowY + 24)
				.size(230, 17)
				.build());

		this.backButton = this.addRenderableWidget(Button.builder(
				Component.literal("BACK"),
				button -> Navigation.open(this.minecraft, new AzyronHomeScreen()))
				.pos(this.width - 82, this.height - 32)
				.size(70, 18)
				.build());
	}

	@Override
	protected void paint(AzyronUi ui, int mouseX, int mouseY, float delta) {
		ScreenPainter.background(ui);

		ui.text("CLIENT SETTINGS", 14, 8, AzyronConstants.COLOR_BLUE);
		ui.text("Optimization behavior and client information", 14, 20, AzyronConstants.COLOR_TEXT_DIM);
		ScreenPainter.divider(ui, ui.width() / 2, 32, ui.width() - 24);

		AzyronClientState state = AzyronClientState.get();

		int panelWidth = Math.min(320, this.width - 24);
		ScreenPainter.panel(ui, 12, this.panelTop, panelWidth, 66, true);
		ui.text("GENERAL", 20, this.panelTop + 5, AzyronConstants.COLOR_TEXT_DIM);
		ui.text(fit(ui, "Sprint is enabled while holding forward (Android friendly).", panelWidth - 16),
				20, this.panelTop + 56, AzyronConstants.COLOR_TEXT_DIM);

		int aboutTop = this.panelTop + 84;
		// Keep the about panel (and its last text line) above the back button.
		int aboutHeight = Math.max(40, this.height - 44 - aboutTop);
		ScreenPainter.panel(ui, 12, aboutTop, panelWidth, aboutHeight, false);
		int sx = 20;
		int line = aboutTop + 8;
		ui.text("ABOUT", sx, line, AzyronConstants.COLOR_TEXT_DIM);
		line += 14;

		ui.text("AZYRON CLIENT V3", sx, line, AzyronConstants.COLOR_BLUE);
		line += 11;
		ui.text("VERSION   " + AzyronConstants.MOD_VERSION, sx, line, AzyronConstants.COLOR_TEXT);
		line += 11;
		ui.text("DEVELOPER " + AzyronConstants.DEVELOPER, sx, line, AzyronConstants.COLOR_TEXT);
		line += 11;
		ui.text("MINECRAFT " + state.minecraftVersion(), sx, line, AzyronConstants.COLOR_TEXT);
		line += 11;
		ui.text("PROFILE   " + FpsBoostProfile.fromName(state.config().fpsProfile).label, sx, line, AzyronConstants.COLOR_TEXT);
		line += 11;
		ui.text("LOADER    Fabric " + fabricLoaderVersion(), sx, line, AzyronConstants.COLOR_TEXT);
		line += 11;
		ui.text("MODS      " + state.loadedMods().size() + " loaded", sx, line, AzyronConstants.COLOR_TEXT);
		line += 14;
		// Only draw the credit lines that fit above the panel's bottom edge.
		String panelBottom = "Dynamic FPS) ship in the AZYRON modpack, not in this jar.";
		String[] credits = {
				"Performance mods (Sodium, Lithium, FerriteCore,",
				"ImmediatelyFast, Entity Culling, More Culling,",
				panelBottom,
		};
		int limit = aboutTop + aboutHeight - 8;
		for (String credit : credits) {
			if (line + 6 > limit) {
				break;
			}
			ui.text(fit(ui, credit, panelWidth - 16), sx, line, AzyronConstants.COLOR_TEXT_DIM);
			line += 10;
		}

		ScreenPainter.footer(ui);
	}

	private static String fit(AzyronUi ui, String text, int maxWidth) {
		if (ui.textWidth(text) <= maxWidth) {
			return text;
		}
		String trimmed = text;
		while (trimmed.length() > 4 && ui.textWidth(trimmed + "...") > maxWidth) {
			trimmed = trimmed.substring(0, trimmed.length() - 1);
		}
		return trimmed + "...";
	}

	private static String fabricLoaderVersion() {
		try {
			return FabricLoader.getInstance().getModContainer("fabricloader")
					.map(container -> container.getMetadata().getVersion().getFriendlyString())
					.orElse("unknown");
		} catch (Throwable t) {
			return "unknown";
		}
	}

	private String autoSprintLabel() {
		return "AUTO SPRINT: " + (AzyronClientState.get().config().autoSprint ? "ON" : "OFF");
	}

	private String autoApplyLabel() {
		return "OPTIMIZE ON FIRST LAUNCH: " + (AzyronClientState.get().config().autoApplyOptimization ? "ON" : "OFF");
	}
}
