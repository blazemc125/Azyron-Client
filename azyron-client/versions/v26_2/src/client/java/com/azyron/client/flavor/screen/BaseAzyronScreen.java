package com.azyron.client.flavor.screen;

import com.azyron.client.client.AzyronUi;
import com.azyron.client.flavor.ui.GuiGraphicsExtractorUi;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Base class bridging the 26.x (GuiGraphicsExtractor) Screen API
 * onto the shared AzyronUi abstraction.
 *
 * <p>Background handling: the game already draws the screen background (and,
 * with a world loaded, the menu blur) as part of the normal screen render.
 * Asking for it a second time inside the same frame throws
 * {@code IllegalStateException: Can only blur once per frame}, which crashed
 * every AZYRON screen on 1.21.11.
 *
 * <p>The fix requests the background at most once per frame. The visual result
 * is unchanged: the background is drawn once, AZYRON's own opaque backdrop
 * covers it, then the content is painted.
 */
public abstract class BaseAzyronScreen extends Screen {

	/** Frame stamp the background bookkeeping below belongs to. */
	private long azyeronFrame = Long.MIN_VALUE;

	/**
	 * Whether this screen already used this frame's single background pass.
	 * A per-screen field, not a static one: a shared flag would leak between
	 * screens during a transition and rob the next screen of its background.
	 */
	private boolean azyeronBackgroundDrawn;

	protected BaseAzyronScreen() {
		super(Component.literal("AZYRON CLIENT V3"));
	}

	protected abstract void paint(AzyronUi ui, int mouseX, int mouseY, float delta);

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		if (beginFrame()) {
			// Exactly one background/blur pass for this screen frame.
			this.extractBackground(graphics, mouseX, mouseY, delta);
		}
		paint(new GuiGraphicsExtractorUi(graphics), mouseX, mouseY, delta);
		super.extractRenderState(graphics, mouseX, mouseY, delta);
	}

	/** @return true when this is the first render of the current frame. */
	private boolean beginFrame() {
		long now = frameStamp();
		if (now != this.azyeronFrame) {
			this.azyeronFrame = now;
			this.azyeronBackgroundDrawn = false;
		}
		if (this.azyeronBackgroundDrawn) {
			return false;
		}
		this.azyeronBackgroundDrawn = true;
		return true;
	}

	/**
	 * Monotonic stamp that advances roughly once per frame. System.nanoTime is
	 * used because the game's frame counter was renamed between versions; two
	 * renders inside one millisecond share a stamp, which makes the guard more
	 * conservative, never less safe.
	 */
	private static long frameStamp() {
		return System.nanoTime() / 1_000_000L;
	}
}
