package ru.deelter.dungeonrefresher.config;

import com.destroystokyo.paper.MaterialSetTag;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.block.Block;
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
	private final boolean useDispensers;
	private final boolean useDroppers;
	private final boolean clearInventoryOnRefresh;
	private final boolean usePersistentStorage;
	private final boolean protectionEnabled;
	private final String protectionBypassPermission;
	private final boolean protectFromExplosions;
	private final boolean elderGuardianEnabled;
	private final long elderGuardianMinMillis;
	private final long elderGuardianMaxMillis;
	private final boolean resetReplaceLootTable;
	private final String resetReplacementLootTable;
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
		this.useDispensers = config.getBoolean("features.dispensers", true);
		this.useDroppers = config.getBoolean("features.droppers", true);
		this.clearInventoryOnRefresh = config.getBoolean("features.clear-inventory-on-refresh", true);
		this.usePersistentStorage = config.getBoolean("storage.use-persistent-data-container", true);
		this.protectionEnabled = config.getBoolean("protection.enabled", false);
		this.protectionBypassPermission = config.getString("protection.bypass-permission", "dungeonrefresher.bypass");
		this.protectFromExplosions = config.getBoolean("protection.protect-from-explosions", true);
		this.elderGuardianEnabled = config.getBoolean("features.elder-guardian", true);
		this.elderGuardianMinMillis = config.getLong("features.elder-guardian-min-minutes", 1440) * 60 * 1000;
		this.elderGuardianMaxMillis = config.getLong("features.elder-guardian-max-minutes", 2880) * 60 * 1000;
		this.resetReplaceLootTable = config.getBoolean("reset.replace-loot-table", false);
		this.resetReplacementLootTable = config.getString("reset.replacement-loot-table", "minecraft:chests/simple_dungeon");
		this.availableLootTables = filterLootTables();
	}

	public boolean isTrackedContainer(@NonNull Block block) {
		Material type = block.getType();
		if (useChests && (type == Material.CHEST || type == Material.TRAPPED_CHEST || MaterialSetTag.COPPER_CHESTS.isTagged(type))) return true;
		if (useBarrels && type == Material.BARREL) return true;
		if (useVaults && type == Material.VAULT) return true;
		if (useDispensers && type == Material.DISPENSER) return true;
		return useDroppers && type == Material.DROPPER;
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