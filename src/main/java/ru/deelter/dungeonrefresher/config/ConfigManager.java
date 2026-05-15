package ru.deelter.dungeonrefresher.config;

import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.jspecify.annotations.NonNull;
import ru.deelter.dungeonrefresher.DungeonRefresher;

import java.util.ArrayList;
import java.util.List;

@Getter
public class ConfigManager {

	private final long minRefreshMinutes;
	private final long maxRefreshMinutes;
	private final long minRefreshMillis;
	private final long maxRefreshMillis;
	private final boolean useVaults;
	private final boolean useChests;
	private final boolean useBarrels;
	private final boolean clearInventoryOnRefresh;
	private final boolean usePersistentStorage;
	private final boolean protectionEnabled;
	private final String protectionBypassPermission;
	private final boolean protectFromExplosions;
	private final List<String> availableLootTables;

	public ConfigManager(@NonNull DungeonRefresher plugin) {
		FileConfiguration config = plugin.getConfig();
		this.minRefreshMinutes = config.getLong("refresh.min-minutes", 1440);
		this.maxRefreshMinutes = config.getLong("refresh.max-minutes", 5760);

		this.minRefreshMillis = this.minRefreshMinutes * 60 * 1000;
		this.maxRefreshMillis = this.maxRefreshMinutes * 60 * 1000;
		this.useVaults = config.getBoolean("features.vaults", true);
		this.useChests = config.getBoolean("features.chests", true);
		this.useBarrels = config.getBoolean("features.barrels", true);
		this.clearInventoryOnRefresh = config.getBoolean("features.clear-inventory-on-refresh", true);
		this.usePersistentStorage = config.getBoolean("storage.use-persistent-data-container", true);
		this.protectionEnabled = config.getBoolean("protection.enabled", false);
		this.protectionBypassPermission = config.getString("protection.bypass-permission", "dungeonrefresher.bypass");
		this.protectFromExplosions = config.getBoolean("protection.protect-from-explosions", true);
		this.availableLootTables = filterLootTables();
	}

	private @NonNull List<String> filterLootTables() {
		List<String> result = new ArrayList<>();
		for (org.bukkit.loot.LootTables lootTable : org.bukkit.loot.LootTables.values()) {
			String key = lootTable.getKey().toString();
			if (key.startsWith("minecraft:chests/") || key.startsWith("minecraft:dispensers/")) {
				result.add(key);
			}
		}
		result.sort(String::compareTo);
		return result;
	}
}