package com.azyron.client.client;

import com.azyron.client.common.AzyronConfig;
import com.azyron.client.common.AzyronConfigManager;
import com.azyron.client.common.AzyronConstants;
import com.azyron.client.common.FpsBoostProfile;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Shared client bootstrap used by every version flavor. Handles state,
 * config persistence, the first-launch optimization pass and the auto
 * sprint tick hook. HUD registration is flavor specific because the two
 * Minecraft versions expose different HUD draw signatures.
 */
public final class AzyronSharedInit {

	private AzyronSharedInit() {
	}

	public static void init() {
		FabricLoader loader = FabricLoader.getInstance();
		Path configPath = loader.getConfigDir().resolve(AzyronConstants.MOD_ID + "-v3.properties");
		AzyronConfigManager configManager = new AzyronConfigManager(configPath);
		AzyronConfig config = configManager.load();

		String minecraftVersion = loader.getModContainer("minecraft")
				.map(container -> container.getMetadata().getVersion().getFriendlyString())
				.orElse("unknown");

		List<String> mods = new ArrayList<>();
		loader.getAllMods().stream()
				.map(container -> container.getMetadata().getName())
				.filter(name -> name != null && !name.isBlank())
				.sorted(Comparator.comparing(String::toLowerCase))
				.forEach(mods::add);

		AzyronClientState.set(new AzyronClientState(config, configManager, minecraftVersion, mods));

		ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
			if (config.autoApplyOptimization && !config.optimizationApplied) {
				var report = GameOptionsBridge.applyDefaultLowGraphics(client);
				config.optimizationApplied = true;
				config.fpsProfile = FpsBoostProfile.POTATO.name();
				AzyronClientState.get().setLastReport(report);
				try {
					configManager.save(config);
				} catch (Exception ignored) {
					// Config save failures are non fatal.
				}
			}
		});

		ClientTickEvents.END_CLIENT_TICK.register(AutoSprintHandler::onClientTick);
	}
}
