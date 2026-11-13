package com.azyron.client.client.screens;

import com.azyron.client.client.AzyronClientState;
import com.azyron.client.client.AzyronUi;
import com.azyron.client.common.AzyronConstants;
import com.azyron.client.common.ColorUtil;
import com.azyron.client.common.HudElementConfig;
import com.azyron.client.common.HudElementSizes;
import com.azyron.client.flavor.screen.BaseAzyronScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * HUD SETTINGS page: per element enable, scale, color and nudge controls.
 * The list on the left selects the element, the panel on the right edits it.
 */
public final class HudSettingsScreen extends BaseAzyronScreen {

	private static final double SCALE_STEP = 0.25;
	private static final double SCALE_MIN = 0.5;
	private static final double SCALE_MAX = 2.0;

	private String selected = "fps";

	private final List<Button> elementButtons = new ArrayList<>();
	private Button toggleButton;
	private Button scaleDownButton, scaleUpButton;
	private Button colorButton;
	private Button moveLeftButton, moveRightButton, moveUpButton, moveDownButton;
	private Button editLayoutButton, resetButton, backButton;

	private int panelTop;
	private int rightX, rightWidth;

	@Override
	protected void init() {
		this.panelTop = 40;
		this.rightX = 128;
		this.rightWidth = Math.max(120, this.width - this.rightX - 12);

		AzyronClientState state = AzyronClientState.get();
		List<HudElementConfig> elements = state.config().elements();

		this.elementButtons.clear();
		// The element list is data driven; keep the buttons inside the window
		// instead of assuming a fixed number of entries.
		int listBottom = this.height - 44;
		for (int i = 0; i < elements.size(); i++) {
			int y = this.panelTop + i * 22;
			if (y + 17 > listBottom) {
				break;
			}
			String id = elements.get(i).id;
			this.elementButtons.add(this.addRenderableWidget(Button.builder(
					Component.literal(elementLabel(id)),
					button -> {
						this.selected = id;
						this.refreshToggleLabel();
					})
					.pos(12, y)
					.size(104, 17)
					.build()));
		}

		int controlsX = this.rightX + 6;
		int rowY = this.panelTop + 18;

		this.toggleButton = this.addRenderableWidget(Button.builder(
				Component.literal(toggleLabel()),
				button -> {
					HudElementConfig element = selectedElement();
					element.enabled = !element.enabled;
					AzyronClientState.get().saveConfig();
					button.setMessage(Component.literal(toggleLabel()));
				})
				.pos(controlsX, rowY)
				.size(96, 17)
				.build());

		int scaleY = rowY + 30;
		this.scaleDownButton = this.addRenderableWidget(Button.builder(
				Component.literal("A-"),
				button -> adjustScale(-SCALE_STEP))
				.pos(controlsX, scaleY)
				.size(24, 17)
				.build());
		this.scaleUpButton = this.addRenderableWidget(Button.builder(
				Component.literal("A+"),
				button -> adjustScale(+SCALE_STEP))
				.pos(controlsX + 30, scaleY)
				.size(24, 17)
				.build());

		int colorY = scaleY + 30;
		this.colorButton = this.addRenderableWidget(Button.builder(
				Component.literal("CYCLE COLOR"),
				button -> {
					HudElementConfig element = selectedElement();
					element.color = ColorUtil.nextPreset(element.color);
					AzyronClientState.get().saveConfig();
				})
				.pos(controlsX, colorY)
				.size(96, 17)
				.build());

		int moveY = colorY + 30;
		int moveStep = 8;
		this.moveUpButton = this.addRenderableWidget(Button.builder(
				Component.literal("^"),
				button -> nudge(0, -moveStep))
				.pos(controlsX + 30, moveY)
				.size(20, 17)
				.build());
		this.moveLeftButton = this.addRenderableWidget(Button.builder(
				Component.literal("<"),
				button -> nudge(-moveStep, 0))
				.pos(controlsX, moveY + 20)
				.size(20, 17)
				.build());
		this.moveDownButton = this.addRenderableWidget(Button.builder(
				Component.literal("v"),
				button -> nudge(0, moveStep))
				.pos(controlsX + 30, moveY + 20)
				.size(20, 17)
				.build());
		this.moveRightButton = this.addRenderableWidget(Button.builder(
				Component.literal(">"),
				button -> nudge(moveStep, 0))
				.pos(controlsX + 60, moveY + 20)
				.size(20, 17)
				.build());

		// Bottom row: three buttons share the width so they never overlap on
		// narrow (<=360px) GUI widths, and it stays clear of the footer.
		int rowTop = this.height - 34;
		int rowGap = 8;
		int rowWidth = this.width - 24 - rowGap * 2;
		int editWidth = Math.max(76, (int) (rowWidth * 0.42));
		int backWidth = 70;
		int resetWidth = Math.max(70, rowWidth - editWidth - backWidth);

		this.editLayoutButton = this.addRenderableWidget(Button.builder(
				Component.literal("DRAG LAYOUT EDITOR"),
				button -> this.minecraft.setScreen(new HudEditScreen()))
				.pos(12, rowTop)
				.size(editWidth, 18)
				.build());

		this.resetButton = this.addRenderableWidget(Button.builder(
				Component.literal("RESET POSITIONS"),
				button -> {
					AzyronClientState resetState = AzyronClientState.get();
					resetPositions(resetState);
					resetState.saveConfig();
				})
				.pos(12 + editWidth + rowGap, rowTop)
				.size(resetWidth, 18)
				.build());

		this.backButton = this.addRenderableWidget(Button.builder(
				Component.literal("BACK"),
				button -> this.minecraft.setScreen(new AzyronHomeScreen()))
				.pos(this.width - 12 - backWidth, rowTop)
				.size(backWidth, 18)
				.build());
	}

	@Override
	protected void paint(AzyronUi ui, int mouseX, int mouseY, float delta) {
		ScreenPainter.background(ui);

		ui.text("HUD SETTINGS", 14, 8, AzyronConstants.COLOR_BLUE);
		ui.text("Enable, scale, recolor and reposition every AZYRON HUD element", 14, 20, AzyronConstants.COLOR_TEXT_DIM);
		ScreenPainter.divider(ui, ui.width() / 2, 32, ui.width() - 24);

		hudListBackdrop(ui);

		// Right panel
		int panelHeight = Math.min(192, this.height - this.panelTop - 60);
		ScreenPainter.panel(ui, this.rightX, this.panelTop, this.rightWidth, panelHeight, true);

		int sx = this.rightX + 8;
		HudElementConfig element = selectedElement();
		ui.text(elementLabel(element.id) + " HUD", sx, this.panelTop + 5, AzyronConstants.COLOR_WHITE);

		int scaleY = this.panelTop + 18 + 30;
		ui.text(String.format(Locale.ROOT, "SCALE  %.2f", element.scale), sx + 60, scaleY + 4, AzyronConstants.COLOR_TEXT_DIM);

		int colorY = scaleY + 30;
		ui.text("COLOR", sx, colorY + 4, AzyronConstants.COLOR_TEXT_DIM);
		ui.fill(sx + 42, colorY + 2, sx + 76, colorY + 13, element.color);
		ui.fill(sx + 42, colorY + 2, sx + 76, colorY + 3, AzyronConstants.COLOR_BORDER);

		int moveY = colorY + 30;
		ui.text("MOVE", sx, moveY + 4, AzyronConstants.COLOR_TEXT_DIM);
		ui.text("POSITION  " + (int) element.x + ", " + (int) element.y, sx, moveY + 46, AzyronConstants.COLOR_TEXT_DIM);

		ScreenPainter.footer(ui);
	}

	private void hudListBackdrop(AzyronUi ui) {
		int listHeight = 6 * 22 + 4;
		ScreenPainter.panel(ui, 8, this.panelTop - 2, 112, listHeight, false);
	}

	private void adjustScale(double delta) {
		HudElementConfig element = selectedElement();
		double next = Math.max(SCALE_MIN, Math.min(SCALE_MAX, element.scale + delta));
		element.scale = next;
		AzyronClientState.get().saveConfig();
	}

	private void nudge(int dx, int dy) {
		HudElementConfig element = selectedElement();
		element.x += dx;
		element.y += dy;
		AzyronClientState.get().saveConfig();
	}

	private HudElementConfig selectedElement() {
		return AzyronClientState.get().config().element(this.selected);
	}

	private String toggleLabel() {
		return selectedElement().enabled ? "VISIBLE: ON" : "VISIBLE: OFF";
	}

	private void refreshToggleLabel() {
		if (this.toggleButton != null) {
			this.toggleButton.setMessage(Component.literal(toggleLabel()));
		}
	}

	private static void resetPositions(AzyronClientState state) {
		double y = 6;
		for (HudElementConfig element : state.config().elements()) {
			element.x = 6;
			element.y = y;
			element.scale = 1.0;
			y += HudElementSizes.height(element.id, 1.0) + 3;
		}
	}

	private static String elementLabel(String id) {
		return switch (id) {
			case "fps" -> "FPS";
			case "cps" -> "CPS";
			case "ping" -> "PING";
			case "coords" -> "COORDS";
			case "keystrokes" -> "KEYSTROKES";
			case "armor" -> "ARMOR";
			default -> id.toUpperCase(Locale.ROOT);
		};
	}
}
