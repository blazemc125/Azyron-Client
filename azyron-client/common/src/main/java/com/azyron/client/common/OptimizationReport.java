package com.azyron.client.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Result of applying an FPS BOOST profile or the default low-graphics
 * optimization, shown on the FPS BOOST screen. Never throws.
 */
public final class OptimizationReport {

	private final List<String> lines = new ArrayList<>();

	public void applied(String setting, String target) {
		lines.add("OK   " + setting + " -> " + target);
	}

	public void skipped(String setting, String reason) {
		lines.add("SKIP " + setting + " (" + reason + ")");
	}

	public void failed(String setting, String reason) {
		lines.add("FAIL " + setting + " (" + reason + ")");
	}

	public List<String> lines() {
		return Collections.unmodifiableList(lines);
	}

	public boolean isEmpty() {
		return lines.isEmpty();
	}
}
