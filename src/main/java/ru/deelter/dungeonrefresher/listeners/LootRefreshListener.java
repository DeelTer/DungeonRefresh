package ru.deelter.dungeonrefresher.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.TileState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.LootTable;
import org.bukkit.loot.Lootable;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;
import ru.deelter.dungeonrefresher.DungeonRefresher;
import ru.deelter.dungeonrefresher.utils.CooldownCache;
import ru.deelter.dungeonrefresher.utils.LootRefresher;
import ru.deelter.dungeonrefresher.utils.RandomUtil;

public class LootRefreshListener implements Listener {

	private final DungeonRefresher plugin;
	private final CooldownCache cooldownCache;

	@Contract(pure = true)
	public LootRefreshListener(@NonNull DungeonRefresher plugin) {
		this.plugin = plugin;
		this.cooldownCache = plugin.getCooldownCache();
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onLootGenerate(@NonNull LootGenerateEvent event) {
		if (!(event.getInventoryHolder() instanceof BlockState state)) return;
		if (!isLootable(state.getBlock())) return;

		if (state instanceof TileState tileState) {
			String lootKey = event.getLootTable().getKey().toString();
			var pdc = tileState.getPersistentDataContainer();
			pdc.set(LootRefresher.LOOT_TABLE_KEY, PersistentDataType.STRING, lootKey);
			tileState.update();
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onInventoryOpen(@NonNull InventoryOpenEvent event) {
		Inventory inventory = event.getInventory();
		if (!(inventory.getHolder() instanceof BlockState state)) return;
		Block block = state.getBlock();
		if (!isLootable(block)) return;

		long cooldown = cooldownCache.getCooldown(block);
		long now = System.currentTimeMillis();
		long refreshDelay = getRandomRefreshDelay();

		if (cooldown == 0) {
			cooldownCache.setCooldown(block, now + refreshDelay);
			return;
		}

		if (now >= cooldown) {
			regenerateLoot(state, inventory);
			cooldownCache.setCooldown(block, now + getRandomRefreshDelay());
		}
	}

	private void regenerateLoot(BlockState state, Inventory inventory) {
		if (plugin.getConfigManager().isClearInventoryOnRefresh()) {
			inventory.clear();
		}
		LootTable table = LootRefresher.getEffectiveLootTable(plugin, state);
		if (table == null) return;
		table.fillInventory(inventory, RandomUtil.RANDOM, new LootContext.Builder(state.getLocation()).build());
	}

	private LootTable getStoredLootTable(BlockState state) {
		if (!(state instanceof TileState tileState)) return null;

		PersistentDataContainer container = tileState.getPersistentDataContainer();
		NamespacedKey key = new NamespacedKey(plugin, "loot_table");
		String tableKeyId = container.get(key, PersistentDataType.STRING);
		if (tableKeyId == null) return null;

		var tableKey = NamespacedKey.fromString(tableKeyId);
		if (tableKey == null) return null;

		return Bukkit.getLootTable(tableKey);
	}

	private boolean isLootable(@NonNull Block block) {
		Material type = block.getType();
		var config = plugin.getConfigManager();

		if (!(config.isUseChests() && (type == Material.CHEST || type == Material.TRAPPED_CHEST)) &&
		    !(config.isUseBarrels() && type == Material.BARREL)) {
			return false;
		}

		BlockState state = block.getState();
		// Chest has vanilla loot table (not yet opened) — dungeon chest
		if (state instanceof Lootable lootable && lootable.getLootTable() != null) return true;
		// Chest was opened before — we saved its loot table key in PDC
		return getStoredLootTable(state) != null;
	}

	private long getRandomRefreshDelay() {
		long min = plugin.getConfigManager().getMinRefreshMillis();
		long max = plugin.getConfigManager().getMaxRefreshMillis();
		return RandomUtil.randomLong(min, max);
	}
}