package com.azyron.client.common;

/**
 * Mutable per-element HUD settings. Serialized flat by {@link AzyronConfigManager}.
 */
public final class HudElementConfig {

	public final String id;
	public boolean enabled;
	public double x;
	public double y;
	public double scale;
	public int color;

	public HudElementConfig(String id, boolean enabled, double x, double y, double scale, int color) {
		this.id = id;
		this.enabled = enabled;
		this.x = x;
		this.y = y;
		this.scale = scale;
		this.color = color;
	}
}
