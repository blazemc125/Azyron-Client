package com.azyron.client.client.screens;

import com.azyron.client.client.AzyronClientState;
import com.azyron.client.client.AzyronUi;
import com.azyron.client.client.GameOptionsBridge;
import com.azyron.client.common.AzyronConstants;
import com.azyron.client.common.FpsBoostProfile;
import com.azyron.client.flavor.screen.BaseAzyronScreen;
import com.azyron.client.flavor.ui.Navigation;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * FPS BOOST page: one-click optimization, the POTATO / BALANCED / SMOOTH
 * profiles and a report of the last optimization pass.
 */
public final class FpsBoostScreen extends BaseAzyronScreen {

	private static final FpsBoostProfile[] PROFILES = FpsBoostProfile.values();

	private Button oneClickButton;
	private Button autoApplyButton;
	private Button backButton;
	private final Button[] applyButtons = new Button[PROFILES.length];

	private int headerY;
	private int cardY;
	private int cardHeight = 44;
	private int cardGap = 6;
	private int bottomY;
	private int reportY;

	@Override
	protected void init() {
		this.headerY = 34;
		this.cardY = 66;

		// Vertical budget: header divider -> one-click button -> three profile
		// cards -> auto-apply button -> report block -> back button -> footer.
		int backY = this.height - 32;
		int reservedBelow = 18 + 6 + 18 + 8 + 26 + 10; // one-click, auto-apply, report header+line
		int availableForCards = backY - 4 - this.cardY - reservedBelow;
		int maxCard = 44;
		this.cardGap = availableForCards < 150 ? 4 : 6;
		// Never exceed the budget: a hard floor of 30px would overflow very
		// short windows, so the floor itself is derived from the space left.
		int minimumCard = Math.max(18, Math.min(30, availableForCards / PROFILES.length - this.cardGap));
		this.cardHeight = Math.max(minimumCard,
				Math.min(maxCard, availableForCards / PROFILES.length - this.cardGap));

		int center = this.width / 2;

		this.oneClickButton = this.addRenderableWidget(Button.builder(
				Component.literal("ONE-CLICK OPTIMIZE"),
				button -> {
					var report = GameOptionsBridge.applyDefaultLowGraphics(this.minecraft);
					AzyronClientState state = AzyronClientState.get();
					state.setLastReport(report);
					state.config().optimizationApplied = true;
					state.config().fpsProfile = FpsBoostProfile.POTATO.name();
					state.saveConfig();
				})
				.pos(center - 105, this.headerY)
				.size(210, 18)
				.build());

		for (int i = 0; i < PROFILES.length; i++) {
			int index = i;
			// Centre the APPLY button inside the card, whatever height it got.
			int buttonHeight = Math.max(12, Math.min(16, this.cardHeight - 10));
			int y = this.cardY + i * (this.cardHeight + this.cardGap) + (this.cardHeight - buttonHeight) / 2;
			this.applyButtons[i] = this.addRenderableWidget(Button.builder(
					Component.literal("APPLY"),
					button -> {
						FpsBoostProfile profile = PROFILES[index];
						var report = GameOptionsBridge.apply(this.minecraft, profile);
						AzyronClientState state = AzyronClientState.get();
						state.setLastReport(report);
						state.config().fpsProfile = profile.name();
						state.saveConfig();
					})
					.pos(this.width - 78, y)
					.size(62, buttonHeight)
					.build());
		}

		this.bottomY = Math.min(this.cardY + PROFILES.length * (this.cardHeight + this.cardGap) + 4, backY - 22);
		this.reportY = Math.min(this.bottomY + 26, backY - 10);

		this.autoApplyButton = this.addRenderableWidget(Button.builder(
				Component.literal(autoApplyLabel()),
				button -> {
					AzyronClientState state = AzyronClientState.get();
					state.config().autoApplyOptimization = !state.config().autoApplyOptimization;
					state.saveConfig();
					button.setMessage(Component.literal(autoApplyLabel()));
				})
				.pos(12, this.bottomY)
				.size(Math.min(210, this.width - 24), 18)
				.build());

		this.backButton = this.addRenderableWidget(Button.builder(
				Component.literal("BACK"),
				button -> Navigation.open(this.minecraft, new AzyronHomeScreen()))
				.pos(this.width - 92, backY)
				.size(80, 18)
				.build());
	}

	@Override
	protected void paint(AzyronUi ui, int mouseX, int mouseY, float delta) {
		ScreenPainter.background(ui);

		ui.text("FPS BOOST", 14, 8, AzyronConstants.COLOR_BLUE);
		ui.text("One-click low-graphics optimization and performance profiles", 14, 20, AzyronConstants.COLOR_TEXT_DIM);
		ScreenPainter.divider(ui, ui.width() / 2, this.headerY - 6, ui.width() - 24);

		AzyronClientState state = AzyronClientState.get();
		String active = FpsBoostProfile.fromName(state.config().fpsProfile).name();

		for (int i = 0; i < PROFILES.length; i++) {
			FpsBoostProfile profile = PROFILES[i];
			int y = this.cardY + i * (this.cardHeight + this.cardGap);
			boolean selected = profile.name().equals(active);

			ui.fill(13, y - 1, ui.width() - 12, y + this.cardHeight + 1,
					selected ? AzyronConstants.COLOR_BORDER_BRIGHT : AzyronConstants.COLOR_BORDER);
			ui.fill(14, y, ui.width() - 13, y + this.cardHeight, AzyronConstants.COLOR_PANEL);
			ui.fill(14, y, ui.width() - 13, y + 1, selected ? AzyronConstants.COLOR_BLUE : AzyronConstants.COLOR_BORDER);

			int nameColor = switch (profile) {
				case POTATO -> 0xFFF59E0B;
				case BALANCED -> AzyronConstants.COLOR_BLUE;
				case SMOOTH -> AzyronConstants.COLOR_GOOD;
			};
			// Text stays inside the card: label near the top, spec line at the
			// bottom, description only when there is room for a third row.
			int labelY = y + Math.max(2, (this.cardHeight - 9) / 2 - (this.cardHeight >= 38 ? 6 : 0));
			ui.text(profile.label, 22, labelY, nameColor);
			if (selected) {
				ScreenPainter.tag(ui, "ACTIVE", 22 + ui.textWidth(profile.label) + 6, labelY - 1, AzyronConstants.COLOR_WHITE);
			}
			if (this.cardHeight >= 38) {
				ui.text(fit(ui, profile.description, Math.max(40, ui.width() - 110)), 22, labelY + 11, AzyronConstants.COLOR_TEXT_DIM);
			}
			if (this.cardHeight >= 30) {
				ui.text(fit(ui, specLine(profile), Math.max(40, ui.width() - 110)), 22, y + this.cardHeight - 11, AzyronConstants.COLOR_TEXT);
			}
		}

		int reportTop = this.reportY;
		ui.text("LAST OPTIMIZATION", 12, reportTop, AzyronConstants.COLOR_TEXT_DIM);
		List<String> lines = state.lastReport().lines();
		if (lines.isEmpty()) {
			ui.text(fit(ui, "No optimization applied yet this session.", ui.width() - 24),
					12, reportTop + 12, AzyronConstants.COLOR_TEXT_DIM);
		} else {
			// Only as many lines as fit before the back button (backY = height - 32).
			int fitLines = (this.height - 32 - 4 - (reportTop + 12)) / 10;
			int max = Math.min(lines.size(), Math.min(4, Math.max(0, fitLines)));
			for (int i = 0; i < max; i++) {
				String line = lines.get(lines.size() - max + i);
				int color = line.startsWith("OK") ? AzyronConstants.COLOR_GOOD
						: line.startsWith("SKIP") ? AzyronConstants.COLOR_WARN
						: AzyronConstants.COLOR_BAD;
				// Trim to fit small screens.
				ui.text(fit(ui, line, ui.width() - 24), 12, reportTop + 12 + i * 10, color);
			}
		}
	}

	private static String specLine(FpsBoostProfile profile) {
		return "RD " + profile.renderDistance + "  SD " + profile.simulationDistance
				+ "  FPS " + profile.maxFps
				+ "  Mipmap " + profile.mipmapLevels
				+ "  Entities " + (int) (profile.entityDistanceScale * 100) + "%"
				+ (profile.particlesMinimal ? "  Particles minimal" : "")
				+ (profile.clouds ? "  Clouds on" : "  Clouds off");
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

	private String autoApplyLabel() {
		boolean enabled = AzyronClientState.get().config().autoApplyOptimization;
		return "AUTO-APPLY ON LAUNCH: " + (enabled ? "ON" : "OFF");
	}
}
