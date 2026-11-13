package com.azyron.client.common;

/**
 * Built in FPS BOOST profiles.
 *
 * POTATO reproduces the exact low graphics specification of AZYRON CLIENT V3
 * (render distance 2, simulation distance 2, clouds off, particles minimal,
 * smooth lighting off, entity distance 50%, mipmap 0, vsync off, max FPS 60,
 * biome blend 0, entity shadows off) and is the recommended profile for
 * low-end Android devices running PojavLauncher.
 */
public enum FpsBoostProfile {

	POTATO("POTATO", "Potato PC / low-end Android", 2, 2, 60, 0, 0,
			false, true, false, false, 0.5),
	BALANCED("BALANCED", "Balanced quality and FPS", 6, 4, 144, 2, 0,
			false, false, false, false, 0.75),
	SMOOTH("SMOOTH", "Smoother frame pacing, more visuals", 8, 6, 120, 4, 2,
			true, false, true, true, 1.0);

	public final String label;
	public final String description;
	public final int renderDistance;
	public final int simulationDistance;
	public final int maxFps;
	public final int mipmapLevels;
	public final int biomeBlend;
	public final boolean clouds;
	public final boolean particlesMinimal;
	public final boolean smoothLighting;
	public final boolean entityShadows;
	public final double entityDistanceScale;

	FpsBoostProfile(String label, String description, int renderDistance, int simulationDistance,
			int maxFps, int mipmapLevels, int biomeBlend, boolean clouds, boolean particlesMinimal,
			boolean smoothLighting, boolean entityShadows, double entityDistanceScale) {
		this.label = label;
		this.description = description;
		this.renderDistance = renderDistance;
		this.simulationDistance = simulationDistance;
		this.maxFps = maxFps;
		this.mipmapLevels = mipmapLevels;
		this.biomeBlend = biomeBlend;
		this.clouds = clouds;
		this.particlesMinimal = particlesMinimal;
		this.smoothLighting = smoothLighting;
		this.entityShadows = entityShadows;
		this.entityDistanceScale = entityDistanceScale;
	}

	public static FpsBoostProfile fromName(String name) {
		if (name != null) {
			for (FpsBoostProfile profile : values()) {
				if (profile.name().equalsIgnoreCase(name) || profile.label.equalsIgnoreCase(name)) {
					return profile;
				}
			}
		}
		return BALANCED;
	}
}
