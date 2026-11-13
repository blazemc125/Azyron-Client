package com.azyron.client.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Properties;

/**
 * Flat properties file persistence for {@link AzyronConfig}.
 * No external dependencies; safe on Java 21 and 25.
 */
public final class AzyronConfigManager {

	private final Path path;

	public AzyronConfigManager(Path path) {
		this.path = path;
	}

	public Path path() {
		return path;
	}

	public AzyronConfig load() {
		AzyronConfig config = new AzyronConfig();
		Properties props = new Properties();
		if (Files.isRegularFile(path)) {
			try (InputStream in = Files.newInputStream(path)) {
				props.load(in);
			} catch (IOException e) {
				// A broken config must never crash the game; fall back to defaults.
				return config;
			}
		}

		config.autoSprint = getBoolean(props, "autoSprint", config.autoSprint);
		config.autoApplyOptimization = getBoolean(props, "autoApplyOptimization", config.autoApplyOptimization);
		config.optimizationApplied = getBoolean(props, "optimizationApplied", config.optimizationApplied);
		config.fpsProfile = props.getProperty("fpsProfile", config.fpsProfile);

		for (HudElementConfig element : config.elements()) {
			String prefix = "hud." + element.id + ".";
			element.enabled = getBoolean(props, prefix + "enabled", element.enabled);
			element.x = getDouble(props, prefix + "x", element.x);
			element.y = getDouble(props, prefix + "y", element.y);
			element.scale = clamp(getDouble(props, prefix + "scale", element.scale), 0.5, 2.0);
			element.color = getColor(props, prefix + "color", element.color);
		}
		return config;
	}

	public void save(AzyronConfig config) {
		Properties props = new Properties();
		props.setProperty("autoSprint", Boolean.toString(config.autoSprint));
		props.setProperty("autoApplyOptimization", Boolean.toString(config.autoApplyOptimization));
		props.setProperty("optimizationApplied", Boolean.toString(config.optimizationApplied));
		props.setProperty("fpsProfile", config.fpsProfile);

		for (HudElementConfig element : config.elements()) {
			String prefix = "hud." + element.id + ".";
			props.setProperty(prefix + "enabled", Boolean.toString(element.enabled));
			props.setProperty(prefix + "x", String.format(Locale.ROOT, "%.1f", element.x));
			props.setProperty(prefix + "y", String.format(Locale.ROOT, "%.1f", element.y));
			props.setProperty(prefix + "scale", String.format(Locale.ROOT, "%.2f", element.scale));
			props.setProperty(prefix + "color", String.format(Locale.ROOT, "%08X", element.color));
		}

		try {
			Path parent = path.getParent();
			if (parent != null) {
				Files.createDirectories(parent);
			}
			Path tmp = path.resolveSibling(path.getFileName() + ".tmp");
			try (OutputStream out = Files.newOutputStream(tmp)) {
				props.store(out, "AZYRON CLIENT V3 configuration");
			}
			Files.move(tmp, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
		} catch (IOException e) {
			try {
				Files.writeString(path, toPropertiesString(props));
			} catch (IOException inner) {
				throw new UncheckedIOException("Could not save AZYRON config to " + path, inner);
			}
		}
	}

	private static String toPropertiesString(Properties props) {
		StringBuilder sb = new StringBuilder("# AZYRON CLIENT V3 configuration\n");
		for (String name : props.stringPropertyNames()) {
			sb.append(name).append('=').append(props.getProperty(name)).append('\n');
		}
		return sb.toString();
	}

	private static boolean getBoolean(Properties props, String key, boolean fallback) {
		String value = props.getProperty(key);
		return value == null ? fallback : Boolean.parseBoolean(value.trim());
	}

	private static double getDouble(Properties props, String key, double fallback) {
		String value = props.getProperty(key);
		if (value == null) {
			return fallback;
		}
		try {
			return Double.parseDouble(value.trim());
		} catch (NumberFormatException e) {
			return fallback;
		}
	}

	private static int getColor(Properties props, String key, int fallback) {
		String value = props.getProperty(key);
		if (value == null) {
			return fallback;
		}
		try {
			long parsed = Long.parseLong(value.trim(), 16);
			return (int) parsed;
		} catch (NumberFormatException e) {
			return fallback;
		}
	}

	private static double clamp(double value, double min, double max) {
		return Math.max(min, Math.min(max, value));
	}
}
