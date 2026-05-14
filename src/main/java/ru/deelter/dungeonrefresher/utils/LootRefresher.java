package ru.deelter.dungeonrefresher.utils;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.block.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.LootTable;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import ru.deelter.dungeonrefresher.DungeonRefresher;

public final class LootRefresher {

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
		LootTable table = getStoredLootTable(plugin, state);
		if (table == null) return;

		table.fillInventory(inventory, RandomUtil.RANDOM, new LootContext.Builder(state.getLocation()).build());

		long min = plugin.getConfigManager().getMinRefreshMillis();
		long max = plugin.getConfigManager().getMaxRefreshMillis();
		long refreshDelay = RandomUtil.randomLong(min, max);
		long newCooldown = System.currentTimeMillis() + refreshDelay;

		plugin.getCooldownCache().setCooldown(block, newCooldown);
	}

	@Nullable
	private static LootTable getStoredLootTable(DungeonRefresher plugin, BlockState state) {
		if (!(state instanceof TileState tileState)) return null;

		PersistentDataContainer container = tileState.getPersistentDataContainer();
		NamespacedKey key = new NamespacedKey(plugin, "loot_table");
		String tableKeyId = container.get(key, PersistentDataType.STRING);

		if (tableKeyId == null) return null;

		var tableKey = NamespacedKey.fromString(tableKeyId);
		if (tableKey == null) return null;

		return Bukkit.getLootTable(tableKey);
	}
}