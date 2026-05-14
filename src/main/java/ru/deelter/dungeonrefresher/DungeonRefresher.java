package ru.deelter.dungeonrefresher;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import ru.deelter.dungeonrefresher.commands.DungeonRefresherCommand;
import ru.deelter.dungeonrefresher.config.ConfigManager;
import ru.deelter.dungeonrefresher.listeners.LootRefreshListener;
import ru.deelter.dungeonrefresher.listeners.ProtectionListener;
import ru.deelter.dungeonrefresher.utils.CooldownCache;

@Getter
@Setter
public final class DungeonRefresher extends JavaPlugin {

	@Getter
	private static DungeonRefresher instance;
	private ConfigManager configManager;
	private CooldownCache cooldownCache;

	@Override
	public void onEnable() {
		instance = this;
		saveDefaultConfig();

		this.configManager = new ConfigManager(this);
		this.cooldownCache = new CooldownCache(this);

		getServer().getPluginManager().registerEvents(new LootRefreshListener(this), this);

		if (configManager.isProtectionEnabled()) {
			getServer().getPluginManager().registerEvents(new ProtectionListener(this), this);
		}
		PluginCommand command = getCommand("dungeonrefresher");
		if (command != null) {
			DungeonRefresherCommand cmdExecutor = new DungeonRefresherCommand(this);
			command.setExecutor(cmdExecutor);
			command.setTabCompleter(cmdExecutor);
		}
	}
}