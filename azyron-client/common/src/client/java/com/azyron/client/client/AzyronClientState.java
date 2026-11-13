package com.azyron.client.client;

import com.azyron.client.common.AzyronConfig;
import com.azyron.client.common.AzyronConfigManager;
import com.azyron.client.common.AzyronConstants;
import com.azyron.client.common.KeyTracker;
import com.azyron.client.common.OptimizationReport;
import net.fabricmc.loader.api.FabricLoader;

import java.util.Collections;
import java.util.List;

/**
 * Process wide client state: config, input tracking, last optimization report.
 * Created once by the shared initializer, read by HUD and screens.
 */
public final class AzyronClientState {

	private static AzyronClientState instance;

	private final AzyronConfig config;
	private final AzyronConfigManager configManager;
	private final KeyTracker keyTracker = new KeyTracker();
	private final String minecraftVersion;
	private final List<String> loadedMods;
	private OptimizationReport lastReport = new OptimizationReport();

	public AzyronClientState(AzyronConfig config, AzyronConfigManager configManager,
			String minecraftVersion, List<String> loadedMods) {
		this.config = config;
		this.configManager = configManager;
		this.minecraftVersion = minecraftVersion;
		this.loadedMods = loadedMods == null ? List.of() : List.copyOf(loadedMods);
	}

	/**
	 * State for the running client. Screens are only reachable after the
	 * client entrypoint has run, but a mod that draws on the title screen
	 * must never crash on a missing initializer, so a lazy default is
	 * returned instead of throwing.
	 */
	public static AzyronClientState get() {
		AzyronClientState state = instance;
		if (state == null) {
			state = bootstrapDefault();
		}
		return state;
	}

	private static synchronized AzyronClientState bootstrapDefault() {
		AzyronClientState state = instance;
		if (state == null) {
			AzyronConfigManager manager = new AzyronConfigManager(
					FabricLoader.getInstance().getConfigDir()
							.resolve(AzyronConstants.MOD_ID + "-v3.properties"));
			state = new AzyronClientState(manager.load(), manager, "unknown", List.of());
			instance = state;
		}
		return state;
	}

	public static void set(AzyronClientState state) {
		instance = state;
	}

	public AzyronConfig config() {
		return config;
	}

	public AzyronConfigManager configManager() {
		return configManager;
	}

	public KeyTracker keyTracker() {
		return keyTracker;
	}

	public String minecraftVersion() {
		return minecraftVersion;
	}

	public List<String> loadedMods() {
		return Collections.unmodifiableList(loadedMods);
	}

	public OptimizationReport lastReport() {
		return lastReport;
	}

	public void setLastReport(OptimizationReport report) {
		this.lastReport = report == null ? new OptimizationReport() : report;
	}

	public void saveConfig() {
		configManager.save(config);
	}
}
