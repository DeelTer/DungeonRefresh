package ru.deelter.dungeonrefresher.commands;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import ru.deelter.dungeonrefresher.DungeonRefresher;
import ru.deelter.dungeonrefresher.config.ConfigManager;
import ru.deelter.dungeonrefresher.utils.LootRefresher;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class DungeonRefresherCommand implements CommandExecutor, TabCompleter {

	private final DungeonRefresher plugin;
	private static final List<String> SUBCOMMANDS = Arrays.asList("reload", "debug", "reset", "setloot", "resetloot");

	public DungeonRefresherCommand(DungeonRefresher plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NonNull [] args) {
		if (args.length == 0) {
			sender.sendMessage(Component.text("Usage: /" + label + " reload | debug | reset | setloot | resetloot"));
			return true;
		}

		if (!(sender instanceof Player player)) {
			sender.sendMessage(Component.text("Only players can execute this command."));
			return true;
		}

		switch (args[0].toLowerCase()) {
			case "reload":
				if (!player.hasPermission("dungeonrefresher.reload")) {
					player.sendMessage(Component.text("You don't have permission!"));
					return true;
				}
				plugin.reloadConfig();
				plugin.setConfigManager(new ConfigManager(plugin));
				plugin.getCooldownCache().invalidateAll();
				player.sendMessage(Component.text("DungeonRefresher configuration reloaded!"));
				break;

			case "debug":
				if (!player.hasPermission("dungeonrefresher.debug")) {
					player.sendMessage(Component.text("You don't have permission!"));
					return true;
				}
				Block targetBlock = player.getTargetBlock(null, 10);
				if (!isLootable(targetBlock)) {
					player.sendMessage(Component.text("You are not looking at a valid loot container!"));
					return true;
				}
				long cooldown = plugin.getCooldownCache().getCooldown(targetBlock);
				if (cooldown <= 0) {
					player.sendMessage(Component.text("This container has never been opened or has no cooldown yet."));
				} else if (cooldown <= System.currentTimeMillis()) {
					player.sendMessage(Component.text("This container is ready to be refreshed!"));
				} else {
					String formatted = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(cooldown));
					player.sendMessage(Component.text("Next refresh available at: " + formatted));
				}
				break;

			case "reset":
				if (!player.hasPermission("dungeonrefresher.reset")) {
					player.sendMessage(Component.text("You don't have permission!"));
					return true;
				}
				Block resetBlock = player.getTargetBlock(null, 10);
				if (!isLootable(resetBlock)) {
					player.sendMessage(Component.text("You are not looking at a valid loot container!"));
					return true;
				}
				LootRefresher.forceRefresh(plugin, resetBlock);
				player.sendMessage(Component.text("Loot has been forcibly refreshed! New cooldown applied."));
				break;

			case "setloot":
				if (!player.hasPermission("dungeonrefresher.setloot")) {
					player.sendMessage(Component.text("You don't have permission!"));
					return true;
				}
				if (args.length < 2) {
					player.sendMessage(Component.text("Usage: /" + label + " setloot <loot_table_key>"));
					return true;
				}
				Block setBlock = player.getTargetBlock(null, 10);
				if (!isLootable(setBlock)) {
					player.sendMessage(Component.text("You are not looking at a valid loot container!"));
					return true;
				}
				String lootKey = args[1];
				if (!isValidLootTable(lootKey)) {
					player.sendMessage(Component.text("Invalid loot table key! Use tab completion to see available keys."));
					return true;
				}
				LootRefresher.setCustomLootTable(setBlock, lootKey);
				player.sendMessage(Component.text("Loot table set to: " + lootKey));
				break;

			case "resetloot":
				if (!player.hasPermission("dungeonrefresher.resetloot")) {
					player.sendMessage(Component.text("You don't have permission!"));
					return true;
				}
				Block resetLootBlock = player.getTargetBlock(null, 10);
				if (!isLootable(resetLootBlock)) {
					player.sendMessage(Component.text("You are not looking at a valid loot container!"));
					return true;
				}
				LootRefresher.removeCustomLootTable(resetLootBlock);
				player.sendMessage(Component.text("Custom loot table removed. Original will be used on next refresh."));
				break;

			default:
				sender.sendMessage(Component.text("Unknown subcommand. Use: /" + label + " reload | debug | reset | setloot | resetloot"));
				break;
		}
		return true;
	}

	@Override
	public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String @NonNull [] args) {
		if (args.length == 1) {
			String partial = args[0].toLowerCase();
			return SUBCOMMANDS.stream()
					.filter(cmd -> cmd.startsWith(partial))
					.collect(Collectors.toList());
		} else if (args.length == 2 && args[0].equalsIgnoreCase("setloot")) {
			String partial = args[1].toLowerCase();
			return plugin.getConfigManager().getAvailableLootTables().stream()
					.filter(table -> table.toLowerCase().startsWith(partial))
					.collect(Collectors.toList());
		}
		return List.of();
	}

	private boolean isLootable(@NonNull Block block) {
		var config = plugin.getConfigManager();
		var type = block.getType();
		if (config.isUseChests() && (type == Material.CHEST || type == Material.TRAPPED_CHEST)) return true;
		if (config.isUseBarrels() && type == Material.BARREL) return true;
		if (config.isUseDispensers() && type == Material.DISPENSER) return true;
		return config.isUseDroppers() && type == Material.DROPPER;
	}

	private boolean isValidLootTable(String key) {
		return plugin.getConfigManager().getAvailableLootTables().contains(key);
	}
}