package com.azyron.client.common;

import java.util.List;

/**
 * All user facing configuration of AZYRON CLIENT V3.
 * Layout positions are absolute pixels on the scaled GUI surface.
 */
public final class AzyronConfig {

	public boolean autoSprint = true;
	public boolean autoApplyOptimization = true;
	public boolean optimizationApplied = false;
	public String fpsProfile = "BALANCED";

	public final HudElementConfig fps = new HudElementConfig("fps", true, 6, 6, 1.0, AzyronConstants.COLOR_WHITE);
	public final HudElementConfig cps = new HudElementConfig("cps", true, 6, 22, 1.0, AzyronConstants.COLOR_WHITE);
	public final HudElementConfig ping = new HudElementConfig("ping", true, 6, 38, 1.0, AzyronConstants.COLOR_WHITE);
	public final HudElementConfig coords = new HudElementConfig("coords", true, 6, 54, 1.0, AzyronConstants.COLOR_WHITE);
	public final HudElementConfig keystrokes = new HudElementConfig("keystrokes", true, 6, 76, 1.0, AzyronConstants.COLOR_WHITE);
	public final HudElementConfig armor = new HudElementConfig("armor", true, 6, 132, 1.0, AzyronConstants.COLOR_WHITE);

	public List<HudElementConfig> elements() {
		return List.of(fps, cps, ping, coords, keystrokes, armor);
	}

	public HudElementConfig element(String id) {
		for (HudElementConfig element : elements()) {
			if (element.id.equals(id)) {
				return element;
			}
		}
		throw new IllegalArgumentException("Unknown HUD element: " + id);
	}
}
