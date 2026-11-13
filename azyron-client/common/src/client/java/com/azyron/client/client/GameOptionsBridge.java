package com.azyron.client.client;

import com.azyron.client.common.FpsBoostProfile;
import com.azyron.client.common.OptimizationReport;
import net.minecraft.client.Minecraft;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Locale;

/**
 * Applies FPS BOOST profiles to the game options using reflection with
 * candidate field names. Mojang renamed a few option fields between
 * 1.21.11 and the unobfuscated 26.x releases, so every single setting is
 * applied in isolation: if a field disappears on a future version, that one
 * setting is reported as skipped and everything else still applies.
 * The client never crashes because of a missing option.
 */
public final class GameOptionsBridge {

	private GameOptionsBridge() {
	}

	/** The exact default low-graphics specification of AZYRON CLIENT V3. */
	public static OptimizationReport applyDefaultLowGraphics(Minecraft minecraft) {
		return apply(minecraft, FpsBoostProfile.POTATO);
	}

	public static OptimizationReport apply(Minecraft minecraft, FpsBoostProfile profile) {
		OptimizationReport report = new OptimizationReport();

		Object options = minecraft == null ? null : minecraft.options;
		if (options == null) {
			report.failed("Options", "options not available yet");
			return report;
		}

		setInt(options, report, "Render distance", profile.renderDistance, "renderDistance");
		setInt(options, report, "Simulation distance", profile.simulationDistance, "simulationDistance");
		setEnum(options, report, "Graphics", "FAST",
				"net.minecraft.client.GraphicsStatus", "FAST", "graphicsMode");
		setEnum(options, report, "Particles",
				"net.minecraft.client.ParticleStatus", profile.particlesMinimal ? "MINIMAL" : "DECREASED",
				"particles");
		setEnum(options, report, "Clouds",
				"net.minecraft.client.CloudStatus", profile.clouds ? "FAST" : "OFF",
				"cloudStatus", "cloudsType", "clouds");
		setBoolean(options, report, "Smooth lighting", profile.smoothLighting, "smoothLighting");
		setDouble(options, report, "Entity distance", profile.entityDistanceScale, "entityDistanceScaling");
		setInt(options, report, "Mipmap levels", profile.mipmapLevels, "mipmapLevels");
		setBoolean(options, report, "VSync", false, "enableVsync");
		setInt(options, report, "Max FPS", profile.maxFps, "framerateLimit", "maxFramerate");
		setInt(options, report, "Biome blend", profile.biomeBlend, "biomeBlendRadius");
		setBoolean(options, report, "Entity shadows", profile.entityShadows, "entityShadows");

		tryInvokeSave(options, report);
		return report;
	}

	private static void setInt(Object options, OptimizationReport report, String label, int value, String... candidates) {
		applyOption(options, report, label, value, candidates);
	}

	private static void setDouble(Object options, OptimizationReport report, String label, double value, String... candidates) {
		applyOption(options, report, label, value, candidates);
	}

	private static void setBoolean(Object options, OptimizationReport report, String label, boolean value, String... candidates) {
		Object option = findOption(options, report, label, candidates);
		if (option == null) {
			return;
		}
		invokeSet(option, report, label, value);
	}

	private static void setEnum(Object options, OptimizationReport report, String label,
			String enumClassName, String enumConstant, String... candidates) {
		Object option = findOption(options, report, label, candidates);
		if (option == null) {
			return;
		}
		Object constant = resolveEnumConstant(enumClassName, enumConstant);
		if (constant == null) {
			report.failed(label, "enum " + enumConstant + " not found in " + enumClassName);
			return;
		}
		invokeSet(option, report, label, constant);
	}

	private static Object findOption(Object options, OptimizationReport report, String label, String... candidates) {
		for (String name : candidates) {
			Field field = AzyronReflect.findField(options.getClass(), name);
			if (field == null) {
				continue;
			}
			try {
				field.setAccessible(true);
				Object value = field.get(options);
				if (value != null) {
					return value;
				}
			} catch (Throwable t) {
				report.failed(label, "could not read option '" + name + "': " + rootMessage(t));
				return null;
			}
		}
		report.skipped(label, "option not present on this version");
		return null;
	}

	private static void applyOption(Object options, OptimizationReport report, String label, Object value,
			String... candidates) {
		Object option = findOption(options, report, label, candidates);
		if (option == null) {
			return;
		}
		invokeSet(option, report, label, value);
	}

	private static void invokeSet(Object option, OptimizationReport report, String label, Object value) {
		try {
			Method set = null;
			for (Method method : option.getClass().getMethods()) {
				if (method.getName().equals("set") && method.getParameterCount() == 1) {
					Class<?> param = method.getParameterTypes()[0];
					if (param.isPrimitive() || Number.class.isAssignableFrom(param) || param.isInstance(value)
							|| Boolean.class.isAssignableFrom(param) || Integer.class.isAssignableFrom(param)
							|| Double.class.isAssignableFrom(param)) {
						set = method;
						break;
					}
				}
			}
			if (set == null) {
				report.failed(label, "no set(...) method on option");
				return;
			}
			set.setAccessible(true);
			set.invoke(option, convertValue(value, set.getParameterTypes()[0]));
			report.applied(label, describe(value));
		} catch (Throwable t) {
			report.failed(label, rootMessage(t));
		}
	}

	private static Object convertValue(Object value, Class<?> target) {
		if (target.isInstance(value)) {
			return value;
		}
		if (value instanceof Number number) {
			if (target == int.class || target == Integer.class) {
				return number.intValue();
			}
			if (target == double.class || target == Double.class) {
				return number.doubleValue();
			}
			if (target == float.class || target == Float.class) {
				return number.floatValue();
			}
			if (target == long.class || target == Long.class) {
				return number.longValue();
			}
		}
		if (value instanceof Boolean bool) {
			if (target == boolean.class || target == Boolean.class) {
				return bool;
			}
		}
		if (target.isEnum() && value instanceof String string) {
			@SuppressWarnings({"unchecked", "rawtypes"})
			Object constant = Enum.valueOf((Class<? extends Enum>) target, string.toUpperCase(Locale.ROOT));
			return constant;
		}
		throw new IllegalArgumentException("Cannot convert " + value.getClass().getSimpleName() + " to " + target.getSimpleName());
	}

	private static Object resolveEnumConstant(String enumClassName, String constant) {
		try {
			Class<?> enumClass = Class.forName(enumClassName);
			if (!enumClass.isEnum()) {
				return null;
			}
			@SuppressWarnings({"unchecked", "rawtypes"})
			Object match = Enum.valueOf((Class<? extends Enum>) enumClass, constant);
			return match;
		} catch (Throwable t) {
			return null;
		}
	}

	private static void tryInvokeSave(Object options, OptimizationReport report) {
		try {
			Method save = options.getClass().getMethod("save");
			save.setAccessible(true);
			save.invoke(options);
			report.applied("Options", "saved");
		} catch (Throwable t) {
			report.failed("Options", "could not save: " + rootMessage(t));
		}
	}

	private static String describe(Object value) {
		if (value instanceof Double d) {
			return String.format(Locale.ROOT, "%.2f", d);
		}
		return String.valueOf(value);
	}

	private static String rootMessage(Throwable t) {
		Throwable cause = t;
		while (cause.getCause() != null) {
			cause = cause.getCause();
		}
		String message = cause.getMessage();
		return cause.getClass().getSimpleName() + (message == null ? "" : ": " + message);
	}
}
