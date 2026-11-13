package com.azyron.client.client.screens;

import com.azyron.client.client.AzyronClientState;
import com.azyron.client.client.AzyronUi;
import com.azyron.client.common.AzyronConstants;
import com.azyron.client.common.FpsBoostProfile;
import com.azyron.client.flavor.screen.BaseAzyronScreen;
import com.azyron.client.flavor.ui.Navigation;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

/**
 * AZYRON CLIENT V3 home screen: premium dark hub with navigation and
 * a live status panel. Replaces nothing: vanilla buttons stay intact.
 */
public final class AzyronHomeScreen extends BaseAzyronScreen {

	private Button boostButton;
	private Button hudEditButton;
	private Button hudSettingsButton;
	private Button versionsButton;
	private Button clientSettingsButton;
	private Button backButton;

	private boolean compact;
	private int navX, navY, navWidth, panelTop, panelHeight;
	private int statusX, statusWidth;

	@Override
	protected void init() {
		this.compact = this.width < 430;
		// Five nav buttons at 17px + 5px gap need 105px under the 18px header.
		// Low GUI scales (240-270px tall) cannot fit that plus the footer, so the
		// panels shrink and the buttons keep a 15px minimum height.
		int buttonHeight = this.height >= 300 ? 17 : 15;
		int step = buttonHeight + 5;
		int navHeader = 18;
		int needed = navHeader + step * 5 + 4;

		this.panelTop = this.compact ? (this.height >= 300 ? 74 : 64) : 78;
		int available = this.height - this.panelTop - 34;
		this.panelHeight = Math.max(needed, Math.min(150, available));
		this.navX = 12;
		this.navY = this.panelTop + navHeader;
		this.navWidth = this.compact ? 116 : 150;
		this.statusX = this.navX + this.navWidth + 8;
		this.statusWidth = Math.max(90, this.width - this.statusX - 12);

		this.boostButton = this.addRenderableWidget(Button.builder(
				Component.literal("FPS BOOST"),
				button -> Navigation.open(this.minecraft, new FpsBoostScreen()))
				.pos(this.navX + 5, this.navY)
				.size(this.navWidth - 10, buttonHeight)
				.build());

		this.hudEditButton = this.addRenderableWidget(Button.builder(
				Component.literal("HUD LAYOUT"),
				button -> Navigation.open(this.minecraft, new HudEditScreen()))
				.pos(this.navX + 5, this.navY + step)
				.size(this.navWidth - 10, buttonHeight)
				.build());

		this.hudSettingsButton = this.addRenderableWidget(Button.builder(
				Component.literal("HUD SETTINGS"),
				button -> Navigation.open(this.minecraft, new HudSettingsScreen()))
				.pos(this.navX + 5, this.navY + step * 2)
				.size(this.navWidth - 10, buttonHeight)
				.build());

		this.versionsButton = this.addRenderableWidget(Button.builder(
				Component.literal("VERSIONS"),
				button -> Navigation.open(this.minecraft, new VersionSelectScreen()))
				.pos(this.navX + 5, this.navY + step * 3)
				.size(this.navWidth - 10, buttonHeight)
				.build());

		this.clientSettingsButton = this.addRenderableWidget(Button.builder(
				Component.literal("CLIENT SETTINGS"),
				button -> Navigation.open(this.minecraft, new ClientSettingsScreen()))
				.pos(this.navX + 5, this.navY + step * 4)
				.size(this.navWidth - 10, buttonHeight)
				.build());

		int backY = Math.min(this.panelTop + this.panelHeight + 8, this.height - 32);
		this.backButton = this.addRenderableWidget(Button.builder(
				Component.literal("BACK TO MENU"),
				button -> this.onClose())
				.pos(this.width / 2 - 60, backY)
				.size(120, 18)
				.build());
	}

	@Override
	protected void paint(AzyronUi ui, int mouseX, int mouseY, float delta) {
		ScreenPainter.background(ui);

		float titleScale = this.compact ? 1.8f : 2.4f;
		ScreenPainter.wordmark(ui, ui.width() / 2, 10, titleScale);
		ui.centeredText(AzyronConstants.TAGLINE, ui.width() / 2, this.compact ? 46 : 50, AzyronConstants.COLOR_TEXT_DIM);
		ScreenPainter.divider(ui, ui.width() / 2, this.compact ? 58 : 62, this.compact ? 90 : 130);

		AzyronClientState state = AzyronClientState.get();

		// Left navigation panel
		ScreenPainter.panel(ui, this.navX, this.panelTop, this.navWidth, this.panelHeight, true);
		ui.text("NAVIGATION", this.navX + 8, this.panelTop + 5, AzyronConstants.COLOR_TEXT_DIM);

		// Right status panel
		ScreenPainter.panel(ui, this.statusX, this.panelTop, this.statusWidth, this.panelHeight, false);
		int sx = this.statusX + 8;
		ui.text("STATUS", sx, this.panelTop + 5, AzyronConstants.COLOR_TEXT_DIM);

		int line = this.panelTop + 18;
		ui.text("MINECRAFT", sx, line, AzyronConstants.COLOR_BLUE);
		ui.text(state.minecraftVersion(), sx + 62, line, AzyronConstants.COLOR_TEXT);
		line += 12;
		ui.text("PROFILE", sx, line, AzyronConstants.COLOR_BLUE);
		ui.text(FpsBoostProfile.fromName(state.config().fpsProfile).label, sx + 62, line, AzyronConstants.COLOR_TEXT);
		line += 12;
		ui.text("SPRINT", sx, line, AzyronConstants.COLOR_BLUE);
		ui.text(state.config().autoSprint ? "AUTO" : "MANUAL", sx + 62, line,
				state.config().autoSprint ? AzyronConstants.COLOR_GOOD : AzyronConstants.COLOR_TEXT_DIM);
		line += 12;
		ui.text("HUD", sx, line, AzyronConstants.COLOR_BLUE);
		long enabled = state.config().elements().stream().filter(element -> element.enabled).count();
		ui.text(enabled + " / " + state.config().elements().size() + " elements", sx + 62, line, AzyronConstants.COLOR_TEXT);
		line += 14;

		ui.text("MODS (" + state.loadedMods().size() + ")", sx, line, AzyronConstants.COLOR_BLUE);
		line += 11;
		StringBuilder mods = new StringBuilder();
		for (String name : state.loadedMods()) {
			if (name.equalsIgnoreCase("Minecraft") || name.equalsIgnoreCase("Fabric Loader")
				|| name.equalsIgnoreCase("Fabric API")) {
				continue;
			}
			if (mods.length() > 0) {
				mods.append(", ");
			}
			mods.append(name);
			if (mods.length() > 88) {
				break;
			}
		}
		String modList = mods.toString();
		int modLines = Math.max(0, (this.panelTop + this.panelHeight - 6 - line) / 10);
		drawWrapped(ui, modList, sx, line, this.statusWidth - 16, AzyronConstants.COLOR_TEXT_DIM, Math.min(3, modLines));

		ScreenPainter.footer(ui);
	}

	private static void drawWrapped(AzyronUi ui, String text, int x, int y, int maxWidth, int argb, int maxLines) {
		String[] words = text.split(" ");
		StringBuilder line = new StringBuilder();
		int lines = 0;
		for (String word : words) {
			String candidate = line.length() == 0 ? word : line + " " + word;
			if (ui.textWidth(candidate) > maxWidth) {
				ui.text(line.toString(), x, y + lines * 10, argb);
				lines++;
				if (lines >= maxLines) {
					return;
				}
				line = new StringBuilder(word);
			} else {
				line = new StringBuilder(candidate);
			}
		}
		if (line.length() > 0 && lines < maxLines) {
			ui.text(line.toString(), x, y + lines * 10, argb);
		}
	}
}
