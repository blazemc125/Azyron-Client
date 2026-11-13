package com.azyron.client.client.screens;

import com.azyron.client.client.AzyronUi;
import com.azyron.client.common.AzyronConstants;

/**
 * Shared paint helpers: background, panels, divider and the AZYRON wordmark.
 * All shapes are flat fills: cheap on weak GPUs (PojavLauncher targets).
 */
public final class ScreenPainter {

	private ScreenPainter() {
	}

	/** Dark premium backdrop with a subtle dot lattice. */
	public static void background(AzyronUi ui) {
		ui.fill(0, 0, ui.width(), ui.height(), AzyronConstants.COLOR_BACKGROUND);

		int dot = 0x14FFFFFF;
		for (int y = 12; y < ui.height(); y += 32) {
			for (int x = 12; x < ui.width(); x += 32) {
				ui.fill(x, y, x + 1, y + 1, dot);
			}
		}
	}

	/** Panel with 1px border and an accent edge on the left. */
	public static void panel(AzyronUi ui, int x, int y, int width, int height, boolean accent) {
		ui.fill(x - 1, y - 1, x + width + 1, y + height + 1, AzyronConstants.COLOR_BORDER);
		ui.fill(x, y, x + width, y + height, AzyronConstants.COLOR_PANEL);
		if (accent) {
			ui.fill(x, y, x + 2, y + height, AzyronConstants.COLOR_PURPLE);
		}
	}

	/** Signature wordmark: stacked electric blue on deep purple. */
	public static void wordmark(AzyronUi ui, int centerX, int y, float scale) {
		ui.pushScale(scale, centerX, y);
		String title = "AZYRON";
		int titleWidth = ui.textWidth(title);
		ui.text(title, (int) (centerX - titleWidth / 2.0f) + 1, y + 1, AzyronConstants.COLOR_PURPLE_DARK);
		ui.text(title, (int) (centerX - titleWidth / 2.0f), y, AzyronConstants.COLOR_BLUE);

		String sub = "C L I E N T   V 3";
		int subWidth = ui.textWidth(sub);
		ui.text(sub, (int) (centerX - subWidth / 2.0f), y + 12, AzyronConstants.COLOR_PURPLE);
		ui.popScale();
	}

	/** Gradient divider, blue fading into purple. */
	public static void divider(AzyronUi ui, int centerX, int y, int width) {
		int left = centerX - width / 2;
		int segment = width / 3;
		ui.fill(left, y, left + segment, y + 1, AzyronConstants.COLOR_BLUE);
		ui.fill(left + segment, y, left + 2 * segment, y + 1, AzyronConstants.COLOR_PURPLE);
		ui.fill(left + 2 * segment, y, left + width, y + 1, AzyronConstants.COLOR_PURPLE_DARK);
	}

	public static void footer(AzyronUi ui) {
		String footer = AzyronConstants.CLIENT_NAME + "  |  " + AzyronConstants.DEVELOPER
				+ "  |  v" + AzyronConstants.MOD_VERSION;
		ui.centeredText(footer, ui.width() / 2, ui.height() - 12, AzyronConstants.COLOR_TEXT_DIM);
	}

	public static void tag(AzyronUi ui, String text, int x, int y, int argb) {
		int width = ui.textWidth(text) + 8;
		ui.fill(x, y, x + width, y + 11, AzyronConstants.COLOR_PURPLE_DARK);
		ui.fill(x, y, x + width, y + 1, AzyronConstants.COLOR_BORDER_BRIGHT);
		ui.text(text, x + 4, y + 2, argb);
	}
}
