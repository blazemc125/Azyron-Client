package com.azyron.client.client;

import net.minecraft.client.Minecraft;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.UUID;

/**
 * Version drift tolerant access to small pieces of Minecraft surface that
 * changed packages or signatures between 1.21.11 and 26.x. Every accessor
 * degrades gracefully instead of crashing the client.
 */
public final class AzyronReflect {

	private AzyronReflect() {
	}

	public static boolean isGuiHidden(Minecraft minecraft) {
		try {
			Object options = minecraft.options;
			Field field = findField(options.getClass(), "hideGui", "hide_gui");
			if (field != null && field.getType() == boolean.class) {
				field.setAccessible(true);
				return field.getBoolean(options);
			}
		} catch (Throwable ignored) {
			// fall through
		}
		return false;
	}

	public static int currentFps(Minecraft minecraft) {
		Integer fps = invokeInt(minecraft, "getFps", "getFramerate", "getCurrentFps");
		return fps == null ? -1 : fps;
	}

	public static int pingMs(Minecraft minecraft) {
		try {
			Object connection = invokeNoArg(minecraft, "getConnection");
			if (connection == null || minecraft.player == null) {
				return -1;
			}
			UUID uuid = minecraft.player.getUUID();
			Object info = invokeOneArg(connection, uuid, "getPlayerInfo");
			if (info == null) {
				return -1;
			}
			Integer latency = invokeInt(info, "getLatency", "getPing", "getLatencyMs");
			return latency == null ? -1 : latency;
		} catch (Throwable t) {
			return -1;
		}
	}

	public static int armorValue(Minecraft minecraft) {
		if (minecraft.player == null) {
			return -1;
		}
		Integer value = invokeInt(minecraft.player, "getArmorValue", "getArmor");
		return value == null ? -1 : value;
	}

	private static Object invokeNoArg(Object target, String... methodNames) {
		for (String name : methodNames) {
			try {
				Method method = target.getClass().getMethod(name);
				method.setAccessible(true);
				return method.invoke(target);
			} catch (Throwable ignored) {
				// try next candidate
			}
		}
		return null;
	}

	private static Object invokeOneArg(Object target, Object arg, String... methodNames) {
		for (String name : methodNames) {
			for (Method method : target.getClass().getMethods()) {
				if (!method.getName().equals(name) || method.getParameterCount() != 1) {
					continue;
				}
				try {
					if (method.getParameterTypes()[0].isInstance(arg)) {
						method.setAccessible(true);
						return method.invoke(target, arg);
					}
				} catch (Throwable ignored) {
					// try next candidate
				}
			}
		}
		return null;
	}

	private static Integer invokeInt(Object target, String... methodNames) {
		Object result = invokeNoArg(target, methodNames);
		if (result instanceof Number number) {
			return number.intValue();
		}
		return null;
	}

	static Field findField(Class<?> start, String... names) {
		Class<?> type = start;
		while (type != null && type != Object.class) {
			for (String name : names) {
				try {
					return type.getDeclaredField(name);
				} catch (NoSuchFieldException ignored) {
					// try next candidate / superclass
				}
			}
			type = type.getSuperclass();
		}
		return null;
	}
}
