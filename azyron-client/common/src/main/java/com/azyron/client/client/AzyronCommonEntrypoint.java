package com.azyron.client.client;

import com.azyron.client.common.AzyronConstants;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common (non client) entrypoint. AZYRON CLIENT V3 is a client side mod;
 * this only logs the loaded profile so dedicated servers can load the jar
 * harmlessly.
 */
public final class AzyronCommonEntrypoint implements ModInitializer {

	public static final Logger LOGGER = LoggerFactory.getLogger(AzyronConstants.MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("{} v{} by {} - Ultimate FPS Optimization", AzyronConstants.CLIENT_NAME,
				AzyronConstants.MOD_VERSION, AzyronConstants.DEVELOPER);
	}
}
