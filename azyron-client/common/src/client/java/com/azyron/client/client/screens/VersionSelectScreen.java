package com.azyron.client.client.screens;

import com.azyron.client.client.AzyronClientState;
import com.azyron.client.client.AzyronUi;
import com.azyron.client.common.AzyronConstants;
import com.azyron.client.flavor.screen.BaseAzyronScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

/**
 * AZYRON builds are per Minecraft version: one jar per version, all built
 * from the same sources. This screen lists every supported build, marks the
 * one currently running (auto detected through the Fabric loader), and
 * explains how to switch. It cannot swap the game version at runtime - no
 * mod can - so switching means launching the matching profile.
 */
public final class VersionSelectScreen extends BaseAzyronScreen {

	private record Build(String mc, String java, String fabricApi) {
		String label() {
			return mc;
		}
	}

	private static final Build[] BUILDS = {
			new Build("1.21.11", "21", "0.141.6+1.21.11"),
			new Build("26.1", "25", "0.145.1+26.1"),
			new Build("26.2", "25", "0.161.0+26.2"),
			new Build("26.3", "25", "0.165.2+26.3"),
	};

	private Button backButton;

	@Override
	protected void init() {
		this.backButton = this.addRenderableWidget(Button.builder(
				Component.literal("BACK"),
				button -> this.minecraft.setScreen(new AzyronHomeScreen()))
				// height - 32 keeps the button clear of the footer line at height - 12.
				.pos(this.width / 2 - 40, this.height - 32)
				.size(80, 18)
				.build());
	}

	@Override
	protected void paint(AzyronUi ui, int mouseX, int mouseY, float delta) {
		ScreenPainter.background(ui);

		ui.text("AZYRON BUILDS", 14, 8, AzyronConstants.COLOR_BLUE);
		ui.text("One build per Minecraft version - detected automatically", 14, 20, AzyronConstants.COLOR_TEXT_DIM);
		ScreenPainter.divider(ui, ui.width() / 2, 32, ui.width() - 24);

		String current = AzyronClientState.get().minecraftVersion();
		boolean compact = this.width < 430;
		int panelWidth = Math.min(340, this.width - 24);

		// The four build rows and the switch instructions must fit between the
		// header divider and the footer; short GUI heights get a tighter row.
		int footerTop = this.height - 34;
		int instructionLines = compact ? 4 : 3;
		int reserved = 14 + instructionLines * 11;
		int rowsSpace = footerTop - 38 - reserved;
		int stride = Math.max(30, Math.min(42, rowsSpace / BUILDS.length));
		int rowHeight = stride - 8;

		int y = 42;
		for (Build build : BUILDS) {
			boolean running = current != null && current.startsWith(build.mc());
			ui.fill(13, y - 1, 12 + panelWidth, y + rowHeight + 1,
					running ? AzyronConstants.COLOR_BORDER_BRIGHT : AzyronConstants.COLOR_BORDER);
			ui.fill(14, y, 11 + panelWidth, y + rowHeight, AzyronConstants.COLOR_PANEL);
			if (running) {
				ui.fill(14, y, 16, y + rowHeight, AzyronConstants.COLOR_PURPLE);
			}

			ui.text("MINECRAFT " + build.label(), 22, y + 4, AzyronConstants.COLOR_WHITE);
			if (running) {
				ScreenPainter.tag(ui, "RUNNING", 22 + ui.textWidth("MINECRAFT " + build.label()) + 6, y + 3, AzyronConstants.COLOR_WHITE);
			}
			if (rowHeight >= 24) {
				ui.text("Java " + build.java() + "   Fabric API " + build.fabricApi(), 22, y + 15, AzyronConstants.COLOR_TEXT_DIM);
			}
			y += stride;
		}

		y = Math.min(y + 2, footerTop - instructionLines * 11);
		ui.text("TO SWITCH BUILD", 14, y, AzyronConstants.COLOR_TEXT_DIM);
		y += 12;
		if (compact) {
			ui.text("Install the AZYRON jar for your", 14, y, AzyronConstants.COLOR_TEXT);
			ui.text("game version, then pick the", 14, y + 11, AzyronConstants.COLOR_TEXT);
			ui.text("matching launcher profile.", 14, y + 22, AzyronConstants.COLOR_TEXT);
			ui.text("Detected: " + (current == null ? "unknown" : current), 14, y + 33, AzyronConstants.COLOR_BLUE);
		} else {
			ui.text("Install the AZYRON jar built for your game version into that profile's mods folder,", 14, y, AzyronConstants.COLOR_TEXT);
			ui.text("then pick the matching profile in PojavLauncher or your PC launcher.", 14, y + 11, AzyronConstants.COLOR_TEXT);
			ui.text("Detected: " + (current == null ? "unknown" : current), 14, y + 24, AzyronConstants.COLOR_BLUE);
		}

		ScreenPainter.footer(ui);
	}
}
