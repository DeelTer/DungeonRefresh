package ru.deelter.dungeonrefresher.utils;

import lombok.experimental.UtilityClass;

import java.util.concurrent.ThreadLocalRandom;

@UtilityClass
public final class RandomUtil {
	public static final ThreadLocalRandom RANDOM = ThreadLocalRandom.current();

	public static long randomLong(long min, long max) {
		if (min >= max) return min;
		return RANDOM.nextLong(min, max + 1);
	}
}