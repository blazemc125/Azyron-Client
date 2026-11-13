package com.azyron.client.common;

/**
 * Unscaled footprint of every HUD element, shared by the renderer, the layout
 * editor and the drag clamp so they cannot disagree about an element's size.
 * Sizes are in GUI pixels at scale 1.
 */
public final class HudElementSizes {

	private HudElementSizes() {
	}

	/** @return {width, height} in GUI pixels at scale 1. */
	public static int[] of(String id) {
		if (id == null) {
			return new int[]{60, 15};
		}
		return switch (id) {
			case "fps" -> new int[]{62, 15};
			case "cps" -> new int[]{74, 15};
			case "ping" -> new int[]{78, 15};
			case "coords" -> new int[]{98, 15};
			case "keystrokes" -> new int[]{57, 66};
			case "armor" -> new int[]{94, 15};
			default -> new int[]{60, 15};
		};
	}

	public static int width(String id, double scale) {
		return (int) Math.round(of(id)[0] * scale);
	}

	public static int height(String id, double scale) {
		return (int) Math.round(of(id)[1] * scale);
	}
}
