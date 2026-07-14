package ru.deelter.dungeonrefresher.utils;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftLootTable;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.inventory.Inventory;
import org.bukkit.loot.LootTable;
import org.jetbrains.annotations.NotNull;

public final class NmsLootFiller {

    private NmsLootFiller() {}

    /**
     * Fills inventory using NMS LootParams with CHEST param set.
     * Handles datapack loot tables whose Bukkit bridge fails due to missing params.
     *
     * @return true if succeeded, false if NMS path also failed
     */
    public static boolean fill(@NotNull LootTable table, @NotNull Inventory inventory,
                               @NotNull Location loc) {
        if (!(loc.getWorld() instanceof CraftWorld craftWorld)) return false;
        if (!(table instanceof CraftLootTable craftTable)) return false;

        ServerLevel level = craftWorld.getHandle();
        Vec3 origin = new Vec3(loc.getX(), loc.getY(), loc.getZ());

        LootParams params = new LootParams.Builder(level)
                .withParameter(LootContextParams.ORIGIN, origin)
                .create(LootContextParamSets.CHEST);

        var nmsTable = craftTable.getHandle();
        var rng = level.getRandom(); // server's RandomSource — close enough for loot

        var items = nmsTable.getRandomItems(params, rng);
        for (var nmsItem : items) {
            var bukkit = CraftItemStack.asBukkitCopy(nmsItem);
            inventory.addItem(bukkit);
        }
        return true;
    }
}
