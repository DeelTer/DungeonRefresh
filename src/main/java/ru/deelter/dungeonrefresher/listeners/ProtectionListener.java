package ru.deelter.dungeonrefresher.listeners;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import ru.deelter.dungeonrefresher.DungeonRefresher;

public class ProtectionListener implements Listener {

	private final DungeonRefresher plugin;

	public ProtectionListener(DungeonRefresher plugin) {
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBlockBreak(@NotNull BlockBreakEvent event) {
		if (!plugin.getConfigManager().isProtectionEnabled()) return;
		Player player = event.getPlayer();
		if (player.hasPermission(plugin.getConfigManager().getProtectionBypassPermission())) return;

		Block block = event.getBlock();
		if (!isProtectedContainer(block)) return;

		long cooldown = plugin.getCooldownCache().getCooldown(block);
		long now = System.currentTimeMillis();
		if (cooldown > 0 && now < cooldown) {
			event.setCancelled(true);
			player.sendMessage(Component.text("This container is protected until its loot refreshes!"));
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onEntityExplode(@NotNull EntityExplodeEvent event) {
		if (!plugin.getConfigManager().isProtectionEnabled()) return;
		if (!plugin.getConfigManager().isProtectFromExplosions()) return;

		event.blockList().removeIf(block -> isProtectedContainer(block));
	}

	private boolean isProtectedContainer(@NonNull Block block) {
		var config = plugin.getConfigManager();
		var type = block.getType();

		if (config.isUseVaults() && type == Material.VAULT) return true;
		if (config.isUseChests() && (type == Material.CHEST || type == Material.TRAPPED_CHEST)) return true;
		return config.isUseBarrels() && type == Material.BARREL;
	}
}