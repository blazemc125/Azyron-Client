package com.azyron.client.common;

import java.util.HashMap;
import java.util.Map;

/**
 * Frame rate sampled input tracker used by the keystrokes and CPS HUD.
 * Pure logic: the client feeds it key booleans sampled from key mappings,
 * so it works identically on every supported Minecraft version.
 */
public final class KeyTracker {

	private static final int CLICK_WINDOW_MS = 1000;
	private static final int CLICK_BUFFER = 64;

	public static final String W = "w";
	public static final String A = "a";
	public static final String S = "s";
	public static final String D = "d";
	public static final String SPACE = "space";
	public static final String LEFT_CLICK = "mouseLeft";
	public static final String RIGHT_CLICK = "mouseRight";

	private static final class ClickRing {
		private final long[] times = new long[CLICK_BUFFER];
		private int cursor;

		void record(long nowMs) {
			times[cursor] = nowMs;
			cursor = (cursor + 1) % CLICK_BUFFER;
		}

		int countSince(long sinceMs) {
			int count = 0;
			for (long time : times) {
				if (time >= sinceMs && time != 0L) {
					count++;
				}
			}
			return count;
		}
	}

	private final Map<String, Boolean> down = new HashMap<>();
	private final Map<String, Long> lastPress = new HashMap<>();
	private final ClickRing leftClicks = new ClickRing();
	private final ClickRing rightClicks = new ClickRing();

	public void updateKey(String key, boolean isDown, long nowMs) {
		boolean wasDown = down.getOrDefault(key, false);
		if (isDown && !wasDown) {
			lastPress.put(key, nowMs);
			if (LEFT_CLICK.equals(key)) {
				leftClicks.record(nowMs);
			} else if (RIGHT_CLICK.equals(key)) {
				rightClicks.record(nowMs);
			}
		}
		down.put(key, isDown);
	}

	public boolean isDown(String key) {
		return down.getOrDefault(key, false);
	}

	public long lastPress(String key) {
		Long value = lastPress.get(key);
		return value == null ? -1L : value;
	}

	public int leftCps(long nowMs) {
		return leftClicks.countSince(nowMs - CLICK_WINDOW_MS);
	}

	public int rightCps(long nowMs) {
		return rightClicks.countSince(nowMs - CLICK_WINDOW_MS);
	}
}
