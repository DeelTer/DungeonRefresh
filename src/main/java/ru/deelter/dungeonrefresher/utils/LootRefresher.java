package ru.deelter.dungeonrefresher.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.LootTable;
import org.bukkit.persistence.PersistentDataType;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import ru.deelter.dungeonrefresher.DungeonRefresher;

public final class LootRefresher {

	private static final NamespacedKey CUSTOM_LOOT_KEY = new NamespacedKey("dungeonrefresher", "custom_loot_table");
	public static final NamespacedKey LOOT_TABLE_KEY = new NamespacedKey("dungeonrefresher", "loot_table");

	public static void forceRefresh(DungeonRefresher plugin, @NonNull Block block) {
		BlockState state = block.getState();
		Inventory inventory = null;

		if (state instanceof Container container) {
			inventory = container.getInventory();
		} else if (state instanceof Vault) {
			plugin.getLogger().warning("Force refresh for Vault is not supported yet.");
			return;
		}
		if (inventory == null) return;

		if (plugin.getConfigManager().isClearInventoryOnRefresh()) {
			inventory.clear();
		}

		LootTable table;
		if (plugin.getConfigManager().isResetReplaceLootTable()) {
			var replacementKey = NamespacedKey.fromString(plugin.getConfigManager().getResetReplacementLootTable());
			table = replacementKey != null ? Bukkit.getLootTable(replacementKey) : null;
			if (table != null) {
				// persist so future auto-refreshes use it too
				setCustomLootTable(block, table.getKey().toString());
			}
		} else {
			table = getEffectiveLootTable(plugin, state);
		}
		if (table == null) return;

		Location loc = state.getLocation();
		try {
			table.fillInventory(inventory, RandomUtil.RANDOM, new LootContext.Builder(loc).build());
		} catch (IllegalArgumentException e) {
			NmsLootFiller.fill(table, inventory, loc);
		}

		long min = plugin.getConfigManager().getMinRefreshMillis();
		long max = plugin.getConfigManager().getMaxRefreshMillis();
		long refreshDelay = RandomUtil.randomLong(min, max);
		long newCooldown = System.currentTimeMillis() + refreshDelay;

		plugin.getCooldownCache().setCooldown(block, newCooldown);
	}

	@Nullable
	public static LootTable getEffectiveLootTable(DungeonRefresher plugin, BlockState state) {
		LootTable custom = getCustomLootTable(state);
		if (custom != null) return custom;
		return getStoredLootTable(plugin, state);
	}

	public static void setCustomLootTable(@NonNull Block block, @NonNull String lootTableKey) {
		if (!(block.getState() instanceof TileState state)) return;
		var pdc = state.getPersistentDataContainer();
		pdc.set(CUSTOM_LOOT_KEY, PersistentDataType.STRING, lootTableKey);
		state.update();
	}

	public static void removeCustomLootTable(@NonNull Block block) {
		if (!(block.getState() instanceof TileState state)) return;
		var pdc = state.getPersistentDataContainer();
		pdc.remove(CUSTOM_LOOT_KEY);
		state.update();
	}

	@Nullable
	private static LootTable getCustomLootTable(BlockState state) {
		if (!(state instanceof TileState tileState)) return null;
		var pdc = tileState.getPersistentDataContainer();
		String tableKeyId = pdc.get(CUSTOM_LOOT_KEY, PersistentDataType.STRING);
		if (tableKeyId == null) return null;
		var tableKey = NamespacedKey.fromString(tableKeyId);
		return tableKey != null ? Bukkit.getLootTable(tableKey) : null;
	}

	public static boolean hasStoredLootTable(@NonNull BlockState state) {
		if (!(state instanceof TileState tileState)) return false;
		var pdc = tileState.getPersistentDataContainer();
		return pdc.has(LOOT_TABLE_KEY, PersistentDataType.STRING);
	}

	@Nullable
	private static LootTable getStoredLootTable(DungeonRefresher plugin, BlockState state) {
		if (!(state instanceof TileState tileState)) return null;

		var pdc = tileState.getPersistentDataContainer();
		String tableKeyId = pdc.get(LOOT_TABLE_KEY, PersistentDataType.STRING);
		if (tableKeyId == null) return null;

		var tableKey = NamespacedKey.fromString(tableKeyId);
		if (tableKey == null) return null;

		return Bukkit.getLootTable(tableKey);
	}
}