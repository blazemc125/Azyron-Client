package com.azyron.client.client.hud;

import com.azyron.client.client.AzyronClientState;
import com.azyron.client.client.AzyronReflect;
import com.azyron.client.client.AzyronUi;
import com.azyron.client.common.AzyronConstants;
import com.azyron.client.common.AzyronConfig;
import com.azyron.client.common.ColorUtil;
import com.azyron.client.common.HudElementConfig;
import com.azyron.client.common.HudElementSizes;
import com.azyron.client.common.KeyTracker;
import net.minecraft.client.Minecraft;

/**
 * Renders every AZYRON HUD element. Drawn once per frame through the
 * {@link AzyronUi} adapter, so it behaves identically on all supported
 * Minecraft versions. Panels are flat, dark and lightweight: no gradients,
 * no textures, no allocations per frame beyond a few strings.
 */
public final class AzyronHudRenderer {

	private static final int PANEL_HEIGHT = 15;
	private static final int PANEL_PADDING = 6;
	private static final int ACCENT_WIDTH = 2;

	private AzyronHudRenderer() {
	}

	public static void render(AzyronUi ui, AzyronClientState state) {
		Minecraft minecraft = Minecraft.getInstance();
		if (AzyronReflect.isGuiHidden(minecraft)) {
			return;
		}

		long now = System.currentTimeMillis();
		sampleInput(minecraft, state.keyTracker(), now);

		AzyronConfig config = state.config();
		for (HudElementConfig element : config.elements()) {
			if (!element.enabled) {
				continue;
			}
			drawElement(ui, state, element, now);
		}
	}

	private static void sampleInput(Minecraft minecraft, KeyTracker tracker, long now) {
		try {
			tracker.updateKey(KeyTracker.W, minecraft.options.keyUp.isDown(), now);
			tracker.updateKey(KeyTracker.A, minecraft.options.keyLeft.isDown(), now);
			tracker.updateKey(KeyTracker.S, minecraft.options.keyDown.isDown(), now);
			tracker.updateKey(KeyTracker.D, minecraft.options.keyRight.isDown(), now);
			tracker.updateKey(KeyTracker.SPACE, minecraft.options.keyJump.isDown(), now);
			tracker.updateKey(KeyTracker.LEFT_CLICK, minecraft.options.keyAttack.isDown(), now);
			tracker.updateKey(KeyTracker.RIGHT_CLICK, minecraft.options.keyUse.isDown(), now);
		} catch (Throwable t) {
			// Input sampling must never break rendering.
		}
	}

	private static void drawElement(AzyronUi ui, AzyronClientState state, HudElementConfig element, long now) {
		// Clamp against the element's own scaled size; a fixed 12px margin
		// clipped tall widgets such as the 66px keystroke grid.
		int width = HudElementSizes.width(element.id, element.scale);
		int height = HudElementSizes.height(element.id, element.scale);
		int x = clamp((int) Math.round(element.x), 0, Math.max(0, ui.width() - width - 2));
		int y = clamp((int) Math.round(element.y), 0, Math.max(0, ui.height() - height - 2));

		ui.pushScale((float) element.scale, x, y);
		switch (element.id) {
			case "fps" -> drawFps(ui, element, x, y, AzyronReflect.currentFps(Minecraft.getInstance()));
			case "cps" -> drawCps(ui, state, element, x, y, now);
			case "ping" -> drawPing(ui, element, x, y, AzyronReflect.pingMs(Minecraft.getInstance()));
			case "coords" -> drawCoords(ui, element, x, y);
			case "keystrokes" -> drawKeystrokes(ui, state, element, x, y, now);
			case "armor" -> drawArmor(ui, element, x, y, AzyronReflect.armorValue(Minecraft.getInstance()));
			default -> { }
		}
		ui.popScale();
	}

	private static int clamp(int value, int min, int max) {
		return Math.max(min, Math.min(max, value));
	}

	private static void panel(AzyronUi ui, int x, int y, int width, int height) {
		ui.fill(x - 1, y - 1, x + width + 1, y + height + 1, AzyronConstants.COLOR_BORDER);
		ui.fill(x, y, x + width, y + height, AzyronConstants.COLOR_PANEL);
		ui.fill(x, y, x + ACCENT_WIDTH, y + height, AzyronConstants.COLOR_PURPLE);
	}

	private static void label(AzyronUi ui, String text, int x, int y) {
		ui.text(text, x, y, AzyronConstants.COLOR_BLUE);
	}

	// Precomputed integer renderings. String.valueOf on every frame allocates on
	// the render thread; on a 3 GB Adreno 610 device that turns into GC stutter.
	private static final String[] SMALL_INTS = new String[512];

	static {
		for (int i = 0; i < SMALL_INTS.length; i++) {
			SMALL_INTS[i] = Integer.toString(i);
		}
	}

	private static String cachedInt(int value) {
		return value >= 0 && value < SMALL_INTS.length ? SMALL_INTS[value] : Integer.toString(value);
	}

	private static void drawFps(AzyronUi ui, HudElementConfig element, int x, int y, int fps) {
		String value = fps > 0 ? cachedInt(fps) : "--";
		int width = PANEL_PADDING + ui.textWidth("FPS") + 4 + ui.textWidth(value) + PANEL_PADDING;
		panel(ui, x, y, width, PANEL_HEIGHT);
		label(ui, "FPS", x + PANEL_PADDING, y + 3);
		ui.text(value, x + PANEL_PADDING + ui.textWidth("FPS") + 4, y + 3, element.color);
	}

	private static void drawCps(AzyronUi ui, AzyronClientState state, HudElementConfig element, int x, int y, long now) {
		KeyTracker tracker = state.keyTracker();
		String left = cachedInt(tracker.leftCps(now));
		String right = cachedInt(tracker.rightCps(now));
		String value = "L " + left + "   R " + right;
		int width = PANEL_PADDING + ui.textWidth("CPS") + 4 + ui.textWidth(value) + PANEL_PADDING;
		panel(ui, x, y, width, PANEL_HEIGHT);
		label(ui, "CPS", x + PANEL_PADDING, y + 3);
		ui.text(value, x + PANEL_PADDING + ui.textWidth("CPS") + 4, y + 3, element.color);
	}

	private static void drawPing(AzyronUi ui, HudElementConfig element, int x, int y, int ping) {
		String value = ping >= 0 ? ping + " ms" : "--";
		int width = PANEL_PADDING + ui.textWidth("PING") + 4 + ui.textWidth(value) + PANEL_PADDING + 10;
		panel(ui, x, y, width, PANEL_HEIGHT);

		int dotX = x + width - 9;
		int dotColor = ping < 0 ? AzyronConstants.COLOR_TEXT_DIM
				: ping <= 60 ? AzyronConstants.COLOR_GOOD
				: ping <= 120 ? AzyronConstants.COLOR_WARN
				: AzyronConstants.COLOR_BAD;
		ui.fill(dotX, y + 5, dotX + 5, y + 10, dotColor);

		ui.text("PING", x + PANEL_PADDING, y + 3, AzyronConstants.COLOR_BLUE);
		ui.text(value, x + PANEL_PADDING + ui.textWidth("PING") + 4, y + 3, element.color);
	}

	private static void drawCoords(AzyronUi ui, HudElementConfig element, int x, int y) {
		Minecraft minecraft = Minecraft.getInstance();
		String value;
		if (minecraft.player != null) {
			value = String.format(java.util.Locale.ROOT, "%d  %d  %d",
					(int) Math.floor(minecraft.player.getX()),
					(int) Math.floor(minecraft.player.getY()),
					(int) Math.floor(minecraft.player.getZ()));
		} else {
			value = "--";
		}
		int width = PANEL_PADDING + ui.textWidth("XYZ") + 4 + ui.textWidth(value) + PANEL_PADDING;
		panel(ui, x, y, width, PANEL_HEIGHT);
		label(ui, "XYZ", x + PANEL_PADDING, y + 3);
		ui.text(value, x + PANEL_PADDING + ui.textWidth("XYZ") + 4, y + 3, element.color);
	}

	private static void drawArmor(AzyronUi ui, HudElementConfig element, int x, int y, int armor) {
		String value = armor >= 0 ? cachedInt(armor) : "--";
		int barWidth = 44;
		int width = PANEL_PADDING + 26 + barWidth + 6 + ui.textWidth(value) + PANEL_PADDING;
		panel(ui, x, y, width, PANEL_HEIGHT);
		ui.text("ARMOR", x + PANEL_PADDING, y + 3, AzyronConstants.COLOR_BLUE);

		int barX = x + PANEL_PADDING + 24;
		int barY = y + 6;
		ui.fill(barX, barY, barX + barWidth, barY + 3, 0xFF2A2547);
		if (armor > 0) {
			int filled = (int) Math.round(barWidth * (Math.min(armor, 20) / 20.0));
			ui.fill(barX, barY, barX + filled, barY + 3, AzyronConstants.COLOR_PURPLE);
			if (filled > 4) {
				ui.fill(barX + filled - 2, barY, barX + filled, barY + 3, AzyronConstants.COLOR_BLUE);
			}
		}
		ui.text(value, barX + barWidth + 3, y + 3, element.color);
	}

	private static void drawKeystrokes(AzyronUi ui, AzyronClientState state, HudElementConfig element, int x, int y, long now) {
		KeyTracker tracker = state.keyTracker();

		// Translucent backdrop for the whole key grid.
		ui.fill(x - 1, y - 1, x + 57, y + 66, ColorUtil.withAlpha(AzyronConstants.COLOR_BACKGROUND, 0xB0));

		key(ui, tracker.isDown(KeyTracker.W), x + 19, y, 16, 16, "W");
		key(ui, tracker.isDown(KeyTracker.A), x, y + 19, 16, 16, "A");
		key(ui, tracker.isDown(KeyTracker.S), x + 19, y + 19, 16, 16, "S");
		key(ui, tracker.isDown(KeyTracker.D), x + 38, y + 19, 16, 16, "D");

		mouseKey(ui, tracker.isDown(KeyTracker.LEFT_CLICK), tracker.leftCps(now), x, y + 38, "L");
		mouseKey(ui, tracker.isDown(KeyTracker.RIGHT_CLICK), tracker.rightCps(now), x + 29, y + 38, "R");
		spaceKey(ui, tracker.isDown(KeyTracker.SPACE), x, y + 55, 57, 10);
	}

	private static void key(AzyronUi ui, boolean down, int x, int y, int w, int h, String label) {
		ui.fill(x, y, x + w, y + h, down ? ColorUtil.withAlpha(AzyronConstants.COLOR_BLUE_DEEP, 0xE6) : AzyronConstants.COLOR_PANEL_RAISED);
		ui.fill(x, y, x + w, y + 1, AzyronConstants.COLOR_BORDER_BRIGHT);
		ui.centeredText(label, x + w / 2, y + (h - 9) / 2, down ? AzyronConstants.COLOR_WHITE : AzyronConstants.COLOR_TEXT);
	}

	private static void mouseKey(AzyronUi ui, boolean down, int cps, int x, int y, String label) {
		ui.fill(x, y, x + 28, y + 15, down ? ColorUtil.withAlpha(AzyronConstants.COLOR_PURPLE_DARK, 0xE6) : AzyronConstants.COLOR_PANEL_RAISED);
		ui.fill(x, y, x + 28, y + 1, AzyronConstants.COLOR_BORDER_BRIGHT);
		ui.text(label, x + 3, y + 3, AzyronConstants.COLOR_TEXT_DIM);
		String cpsText = cachedInt(cps);
		ui.text(cpsText, x + 28 - 4 - ui.textWidth(cpsText), y + 3,
				cps > 0 ? AzyronConstants.COLOR_WHITE : AzyronConstants.COLOR_TEXT_DIM);
	}

	private static void spaceKey(AzyronUi ui, boolean down, int x, int y, int w, int h) {
		ui.fill(x, y, x + w, y + h, down ? ColorUtil.withAlpha(AzyronConstants.COLOR_PURPLE_DARK, 0xE6) : AzyronConstants.COLOR_PANEL_RAISED);
		ui.fill(x, y, x + w, y + 1, AzyronConstants.COLOR_BORDER_BRIGHT);
		ui.centeredText("SPACE", x + w / 2, y + (h - 9) / 2, down ? AzyronConstants.COLOR_WHITE : AzyronConstants.COLOR_TEXT_DIM);
	}
}
