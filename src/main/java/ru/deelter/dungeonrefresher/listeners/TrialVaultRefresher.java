package ru.deelter.dungeonrefresher.listeners;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.vault.VaultBlockEntity;
import net.minecraft.world.level.block.entity.vault.VaultServerData;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import ru.deelter.dungeonrefresher.DungeonRefresher;

import java.lang.reflect.Field;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class TrialVaultRefresher implements Listener {

	private static final long REFRESH_MILLIS = TimeUnit.HOURS.toMillis(24);

	@EventHandler
	public void onVaultInteract(@NotNull PlayerInteractEvent event) {
		if (!DungeonRefresher.getInstance().getConfigManager().isUseVaults()) return;

		Block block = event.getClickedBlock();
		if (block == null || block.getType() != Material.VAULT) {
			return;
		}

		BlockPos blockPos = new BlockPos(block.getX(), block.getY(), block.getZ());
		ServerLevel serverLevel = ((CraftWorld) block.getWorld()).getHandle();
		BlockEntity blockEntity = serverLevel.getBlockEntity(blockPos);
		if (!(blockEntity instanceof VaultBlockEntity vaultBlockEntity)) {
			return;
		}

		VaultServerData vaultServerData = vaultBlockEntity.getServerData();

		PersistentDataContainer dataContainer = blockEntity.persistentDataContainer;
		NamespacedKey key = new NamespacedKey(DungeonRefresher.getInstance(), event.getPlayer().getUniqueId().toString());
		long currentTimestamp = System.currentTimeMillis();

		if (!dataContainer.has(key, PersistentDataType.LONG)) {
			dataContainer.set(key, PersistentDataType.LONG, currentTimestamp);
			return;
		}

		long savedTimestamp = dataContainer.getOrDefault(key, PersistentDataType.LONG, 0L);
		if (currentTimestamp < savedTimestamp + REFRESH_MILLIS) {
			return;
		}

		try {
			Field rewardedPlayersField = VaultServerData.class.getDeclaredField("rewardedPlayers");
			rewardedPlayersField.setAccessible(true);
			@SuppressWarnings("unchecked")
			Set<UUID> rewardedPlayers = (Set<UUID>) rewardedPlayersField.get(vaultServerData);

			UUID playerId = event.getPlayer().getUniqueId();
			if (!rewardedPlayers.contains(playerId)) {
				return;
			}

			rewardedPlayers.remove(playerId);
			event.setCancelled(true);
			event.callEvent();
			dataContainer.remove(key);
		} catch (NoSuchFieldException | IllegalAccessException ex) {
			ex.printStackTrace();
		}
	}


}
