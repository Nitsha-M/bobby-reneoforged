package de.johni0702.minecraft.bobby.util;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Implements the "Flawless Frames" FREX feature using which third-party mods can instruct Bobby to sacrifice
 * performance (even beyond the point where it can no longer achieve interactive frame rates) in exchange for
 * a noticeable boost to quality.
 *
 * In Bobby's case, this means blocking loading all chunks are loaded from the cache.
 *
 * See https://github.com/grondag/frex/pull/9
 */
public class FlawlessFrames {
    private static final Set<Object> ACTIVE = Collections.newSetFromMap(new ConcurrentHashMap<>());

    @SuppressWarnings("unchecked")
    public static void onClientInitialization() {
    }

    public static boolean isActive() {
        return !ACTIVE.isEmpty();
    }
}
