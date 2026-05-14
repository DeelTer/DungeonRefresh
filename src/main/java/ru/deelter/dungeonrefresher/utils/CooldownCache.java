package ru.deelter.dungeonrefresher.utils;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.TileState;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jspecify.annotations.NonNull;
import ru.deelter.dungeonrefresher.DungeonRefresher;

import java.util.concurrent.TimeUnit;

public class CooldownCache {

	private final NamespacedKey refreshKey;
	private final DungeonRefresher plugin;
	private final Cache<String, Long> cache;

	public CooldownCache(@NonNull DungeonRefresher plugin) {
		this.plugin = plugin;
		this.refreshKey = new NamespacedKey(plugin, "refresh_at");

		long maxMinutes = plugin.getConfigManager().getMaxRefreshMinutes();
		this.cache = Caffeine.newBuilder()
				.expireAfterWrite(maxMinutes, TimeUnit.MINUTES)
				.maximumSize(5_000)
				.build();
	}

	public long getCooldown(@NonNull Block block) {
		if (!(block.getState() instanceof TileState state)) return 0L;
		if (plugin.getConfigManager().isUsePersistentStorage()) {
			PersistentDataContainer container = state.getPersistentDataContainer();
			return container.getOrDefault(refreshKey, PersistentDataType.LONG, 0L);
		}
		return cache.get(getKey(block), k -> 0L);
	}

	public void setCooldown(@NonNull Block block, long endTime) {
		if (!(block.getState() instanceof TileState state)) {
			plugin.getLogger().warning("Block is not TileState, cannot save cooldown!");
			return;
		}
		if (plugin.getConfigManager().isUsePersistentStorage()) {
			PersistentDataContainer container = state.getPersistentDataContainer();
			container.set(refreshKey, PersistentDataType.LONG, endTime);
			state.update();
			return;
		}
		cache.put(getKey(block), endTime);
	}

	private @NonNull String getKey(@NonNull Block block) {
		return block.getWorld().getName() + ":" + block.getX() + "_" + block.getY() + "_" + block.getZ();
	}

	public void invalidateAll() {
		cache.invalidateAll();
	}
}