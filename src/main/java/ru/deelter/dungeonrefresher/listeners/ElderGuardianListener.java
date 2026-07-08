package ru.deelter.dungeonrefresher.listeners;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Biome;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import ru.deelter.dungeonrefresher.DungeonRefresher;
import ru.deelter.dungeonrefresher.utils.RandomUtil;

import java.util.ArrayList;
import java.util.List;

public class ElderGuardianListener implements Listener {

	private final DungeonRefresher plugin;
	private final NamespacedKey key;

	public ElderGuardianListener(DungeonRefresher plugin) {
		this.plugin = plugin;
		this.key = new NamespacedKey(plugin, "elder_guardian_spawns");
	}

	@EventHandler
	public void onDeath(EntityDeathEvent event) {
		if (event.getEntityType() != EntityType.ELDER_GUARDIAN) return;

		Location loc = event.getEntity().getLocation();
		if (!isOcean(loc.getBlock().getBiome())) return;

		long respawnAt = System.currentTimeMillis() + RandomUtil.randomLong(
				plugin.getConfigManager().getElderGuardianMinMillis(),
				plugin.getConfigManager().getElderGuardianMaxMillis()
		);

		storeGuardian(loc.getChunk(), loc, respawnAt);
	}

	private boolean isOcean(@NotNull Biome biome) {
		return switch (biome) {
			case OCEAN, COLD_OCEAN, DEEP_COLD_OCEAN, DEEP_FROZEN_OCEAN, DEEP_LUKEWARM_OCEAN, WARM_OCEAN, FROZEN_OCEAN,
			     LUKEWARM_OCEAN, DEEP_OCEAN -> true;
			default -> false;
		};
	}

	@EventHandler
	public void onChunkLoad(ChunkLoadEvent event) {
		checkAndSpawn(event.getChunk());
	}

	private void storeGuardian(Chunk chunk, Location loc, long respawnAt) {
		PersistentDataContainer pdc = chunk.getPersistentDataContainer();
		String existing = pdc.getOrDefault(key, PersistentDataType.STRING, "");
		String entry = loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ() + "," + respawnAt;
		pdc.set(key, PersistentDataType.STRING, existing.isEmpty() ? entry : existing + "|" + entry);
	}

	private void checkAndSpawn(Chunk chunk) {
		PersistentDataContainer pdc = chunk.getPersistentDataContainer();
		String data = pdc.get(key, PersistentDataType.STRING);
		if (data == null || data.isEmpty()) return;

		long now = System.currentTimeMillis();
		List<String> remaining = new ArrayList<>();

		for (String entry : data.split("\\|")) {
			String[] parts = entry.split(",");
			if (parts.length != 4) continue;

			long respawnAt = Long.parseLong(parts[3]);
			if (now >= respawnAt) {
				Location loc = new Location(
						chunk.getWorld(),
						Integer.parseInt(parts[0]) + 0.5,
						Integer.parseInt(parts[1]),
						Integer.parseInt(parts[2]) + 0.5
				);
				chunk.getWorld().spawnEntity(loc, EntityType.ELDER_GUARDIAN);
			} else {
				remaining.add(entry);
			}
		}

		if (remaining.isEmpty()) {
			pdc.remove(key);
		} else {
			pdc.set(key, PersistentDataType.STRING, String.join("|", remaining));
		}
	}
}
