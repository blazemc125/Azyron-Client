package com.azyron.client.common;

/**
 * Small ARGB color helpers shared by HUD and screens.
 */
public final class ColorUtil {

	public static final int[] HUD_PRESETS = {
			0xFFFFFFFF, // white
			0xFF8B5CF6, // azyron purple
			0xFF22D3EE, // electric blue
			0xFF4ADE80, // green
			0xFFFACC15, // yellow
			0xFFFB7185  // red
	};

	private ColorUtil() {
	}

	public static int withAlpha(int argb, int alpha) {
		return (argb & 0x00FFFFFF) | (alpha << 24);
	}

	public static int nextPreset(int current) {
		for (int i = 0; i < HUD_PRESETS.length; i++) {
			if (HUD_PRESETS[i] == current) {
				return HUD_PRESETS[(i + 1) % HUD_PRESETS.length];
			}
		}
		return HUD_PRESETS[0];
	}

	public static String hex(int argb) {
		return String.format("#%06X", argb & 0x00FFFFFF);
	}
}
