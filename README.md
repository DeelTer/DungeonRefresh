# DungeonRefresher

**A lightweight Paper plugin that automatically refreshes loot in chests, barrels, and trial vaults after a configurable cooldown. Perfect for custom dungeons, adventure maps, and multiplayer servers where loot needs to be renewable.**

---

## 🎯 Core Mechanics

- **First‑opening** – when a container is opened for the first time, a random cooldown (between `min` and `max` minutes) is stored.
- **Cooldown expiry** – after the cooldown passes, the container’s loot is completely regenerated on the next open.
- **Custom loot tables** – administrators can assign any vanilla loot table (e.g. `minecraft:chests/simple_dungeon`) to any container using in‑game commands.
- **Persistent data** – cooldowns and custom loot table assignments survive server restarts (using PersistentDataContainer).

## ⚙️ Features

- **Flexible configuration** – choose which container types (chests, barrels, vaults) are affected, min/max cooldown in minutes, and whether to clear inventory before refilling.
- **Storage options** – use PDC (persistent, recommended) or in‑memory Caffeine cache (volatile, faster).
- **Protection system** – optionally prevent players from breaking containers that are still on cooldown (with bypass permission).
- **Commands & permissions** – reload config, check a container's remaining cooldown (`/dr debug`), force‑refresh loot (`/dr reset`), assign custom loot tables (`/dr setloot`), or reset to original loot (`/dr resetloot`).
- **Tab completion** – automatic suggestions for subcommands and all vanilla loot tables (filtered to `minecraft:chests/` and `minecraft:dispensers/`).
- **No external libraries** – uses Paper API, built‑in persistence, and Caffeine (shaded).

## 📋 Commands & Permissions

| Command | Description | Permission |
|---------|-------------|------------|
| `/dr reload` | Reloads config.yml | `dungeonrefresher.reload` |
| `/dr debug` | Shows the next refresh time for the container you're looking at | `dungeonrefresher.debug` |
| `/dr reset` | Instantly refreshes loot and resets cooldown | `dungeonrefresher.reset` |
| `/dr setloot <key>` | Assigns a custom loot table (e.g. `minecraft:chests/simple_dungeon`) to the target container | `dungeonrefresher.setloot` |
| `/dr resetloot` | Removes the custom loot table, restoring the original loot | `dungeonrefresher.resetloot` |

All commands support the alias `/dungeonrefresher`. Permission defaults to `op` (can be changed).

## 🔧 Configuration

All settings are in `config.yml` with detailed comments:

```yaml
# Refresh timing in minutes
refresh:
  min-minutes: 1440   # 24 hours
  max-minutes: 5760   # 96 hours

# Which containers to process
features:
  vaults: true
  chests: true
  barrels: true
  clear-inventory-on-refresh: true

# Storage method
storage:
  use-persistent-data-container: true

# Protection against breaking containers on cooldown
protection:
  enabled: false
  bypass-permission: "dungeonrefresher.bypass"
  protect-from-explosions: true
```
## 📦 Installation
1. Place the DungeonRefresher.jar into your server's plugins/ folder.
2.Restart the server (or run /plugman load DungeonRefresher).
3. (Optional) Edit config.yml to your needs and run /dr reload.