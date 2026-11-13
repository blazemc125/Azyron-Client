package com.azyron.client.client.screens;

import com.azyron.client.client.AzyronClientState;
import com.azyron.client.client.AzyronUi;
import com.azyron.client.common.AzyronConstants;
import com.azyron.client.common.ColorUtil;
import com.azyron.client.common.HudElementConfig;
import com.azyron.client.common.HudElementSizes;
import com.azyron.client.flavor.screen.BaseAzyronScreen;
import com.azyron.client.flavor.ui.Navigation;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Locale;

/**
 * Drag and drop HUD layout editor. Enabled elements are drawn as labeled
 * boxes; drag them anywhere on screen. Layout is saved on exit.
 */
public final class HudEditScreen extends BaseAzyronScreen {

	private Button doneButton;
	private HudElementConfig dragging;
	private double dragOffsetX;
	private double dragOffsetY;
	private String selected = "fps";

	@Override
	protected void init() {
		this.doneButton = this.addRenderableWidget(Button.builder(
				Component.literal("DONE"),
				button -> this.onClose())
				.pos(this.width / 2 - 55, this.height - 32)
				.size(110, 18)
				.build());
	}

	@Override
	public void onClose() {
		AzyronClientState.get().saveConfig();
		Navigation.open(this.minecraft, new HudSettingsScreen());
	}

	@Override
	protected void paint(AzyronUi ui, int mouseX, int mouseY, float delta) {
		ScreenPainter.background(ui);
		ui.text("HUD LAYOUT EDITOR", 14, 8, AzyronConstants.COLOR_BLUE);
		ui.text("Drag any element to reposition it", 14, 20, AzyronConstants.COLOR_TEXT_DIM);
		ScreenPainter.divider(ui, ui.width() / 2, 32, ui.width() - 24);

		for (HudElementConfig element : AzyronClientState.get().config().elements()) {
			if (!element.enabled) {
				continue;
			}
			int w = HudElementSizes.width(element.id, element.scale);
			int h = HudElementSizes.height(element.id, element.scale);
			int x = (int) element.x;
			int y = (int) element.y;

			boolean active = element.id.equals(this.selected);
			ui.fill(x, y, x + w, y + h, ColorUtil.withAlpha(AzyronConstants.COLOR_PANEL_RAISED, 0xCC));
			ui.fill(x, y, x + w, y + 1, active ? AzyronConstants.COLOR_BLUE : AzyronConstants.COLOR_BORDER);
			ui.fill(x, y + h - 1, x + w, y + h, active ? AzyronConstants.COLOR_BLUE : AzyronConstants.COLOR_BORDER);
			ui.fill(x, y, x + 1, y + h, active ? AzyronConstants.COLOR_BLUE : AzyronConstants.COLOR_BORDER);
			ui.fill(x + w - 1, y, x + w, y + h, active ? AzyronConstants.COLOR_BLUE : AzyronConstants.COLOR_BORDER);
			ui.centeredText(element.id.toUpperCase(Locale.ROOT), x + w / 2, y + (h - 9) / 2,
					active ? AzyronConstants.COLOR_WHITE : AzyronConstants.COLOR_TEXT_DIM);
		}
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		List<HudElementConfig> elements = AzyronClientState.get().config().elements();
		double mouseX = event.x();
		double mouseY = event.y();
		for (int i = elements.size() - 1; i >= 0; i--) {
			HudElementConfig element = elements.get(i);
			if (!element.enabled) {
				continue;
			}
			int w = HudElementSizes.width(element.id, element.scale);
			int h = HudElementSizes.height(element.id, element.scale);
			if (mouseX >= element.x && mouseX <= element.x + w && mouseY >= element.y && mouseY <= element.y + h) {
				this.dragging = element;
				this.selected = element.id;
				this.dragOffsetX = mouseX - element.x;
				this.dragOffsetY = mouseY - element.y;
				return true;
			}
		}
		return super.mouseClicked(event, doubleClick);
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
		if (this.dragging != null) {
			int w = HudElementSizes.width(this.dragging.id, this.dragging.scale);
			int h = HudElementSizes.height(this.dragging.id, this.dragging.scale);
			this.dragging.x = clamp(event.x() - this.dragOffsetX, 0, Math.max(0, this.width - w));
			this.dragging.y = clamp(event.y() - this.dragOffsetY, 0, Math.max(0, this.height - h));
			return true;
		}
		return super.mouseDragged(event, dragX, dragY);
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		if (this.dragging != null) {
			this.dragging = null;
			AzyronClientState.get().saveConfig();
			return true;
		}
		return super.mouseReleased(event);
	}

	private static int clamp(double value, int min, int max) {
		return (int) Math.max(min, Math.min(max, Math.round(value)));
	}
}
