package ru.deelter.dungeonrefresher;

import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

public class MetricsManager {

	private final DungeonRefresher plugin;
	private final Metrics metrics;

	public MetricsManager(@NotNull DungeonRefresher plugin, int metricsId) {
		this.plugin = plugin;
		this.metrics = new Metrics(plugin, metricsId);
		setupCustomCharts();
	}

	private void setupCustomCharts() {
		FileConfiguration config = plugin.getConfig();

		// Storage method (PDC or memory)
		metrics.addCustomChart(new SimplePie("storage_method", () ->
				config.getBoolean("storage.use-persistent-data-container", true) ? "pdc" : "memory"
		));

		// Protection enabled?
		metrics.addCustomChart(new SimplePie("protection_enabled", () ->
				config.getBoolean("protection.enabled", false) ? "enabled" : "disabled"
		));

		// Vaults supported?
		metrics.addCustomChart(new SimplePie("vaults_supported", () ->
				config.getBoolean("features.vaults", true) ? "yes" : "no"
		));

		// Chests supported?
		metrics.addCustomChart(new SimplePie("chests_supported", () ->
				config.getBoolean("features.chests", true) ? "yes" : "no"
		));

		// Barrels supported?
		metrics.addCustomChart(new SimplePie("barrels_supported", () ->
				config.getBoolean("features.barrels", true) ? "yes" : "no"
		));

		// Clear inventory on refresh?
		metrics.addCustomChart(new SimplePie("clear_inventory_on_refresh", () ->
				config.getBoolean("features.clear-inventory-on-refresh", true) ? "yes" : "no"
		));

		// Min refresh hours
		metrics.addCustomChart(new SimplePie("min_refresh_hours", () ->
				String.valueOf(config.getLong("refresh.min-minutes", 1440) / 60)
		));

		// Max refresh hours
		metrics.addCustomChart(new SimplePie("max_refresh_hours", () ->
				String.valueOf(config.getLong("refresh.max-minutes", 5760) / 60)
		));
	}
}