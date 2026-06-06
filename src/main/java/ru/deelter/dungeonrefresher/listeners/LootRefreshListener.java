package ru.deelter.dungeonrefresher.listeners;

import org.bukkit.Material;
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
		Block block = state.getBlock();
		if (!isTrackedType(block)) return;

		if (!(state instanceof TileState tileState)) return;

		String lootKey = event.getLootTable().getKey().toString();
		var pdc = tileState.getPersistentDataContainer();
		pdc.set(LootRefresher.LOOT_TABLE_KEY, PersistentDataType.STRING, lootKey);
		tileState.update();

		// Set initial cooldown here — LootGenerateEvent only fires for dungeon containers
		long refreshDelay = getRandomRefreshDelay();
		cooldownCache.setCooldown(block, System.currentTimeMillis() + refreshDelay);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onInventoryOpen(@NonNull InventoryOpenEvent event) {
		Inventory inventory = event.getInventory();
		if (!(inventory.getHolder() instanceof BlockState state)) return;
		Block block = state.getBlock();
		if (!isTrackedType(block)) return;

		// Only act on chests we've already tracked (have PDC key from LootGenerateEvent)
		BlockState freshState = block.getState();
		if (!LootRefresher.hasStoredLootTable(freshState)) return;

		long cooldown = cooldownCache.getCooldown(block);
		long now = System.currentTimeMillis();

		if (cooldown == 0) return; // Should not happen, but skip safely

		if (now >= cooldown) {
			regenerateLoot(freshState, inventory);
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

	private boolean isTrackedType(@NonNull Block block) {
		Material type = block.getType();
		var config = plugin.getConfigManager();

		if (config.isUseChests() && (type == Material.CHEST || type == Material.TRAPPED_CHEST)) return true;
		return config.isUseBarrels() && type == Material.BARREL;
	}

	private long getRandomRefreshDelay() {
		long min = plugin.getConfigManager().getMinRefreshMillis();
		long max = plugin.getConfigManager().getMaxRefreshMillis();
		return RandomUtil.randomLong(min, max);
	}
}
