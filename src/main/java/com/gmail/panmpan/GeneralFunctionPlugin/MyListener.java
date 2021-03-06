package com.gmail.panmpan.GeneralFunctionPlugin;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

public class MyListener implements Listener {
    private final GeneralFunctionPlugin plugin;

    public int playersInSleep = 0;
    public BukkitScheduler scheduler;
	public BukkitTask morningTask;

    public MyListener(GeneralFunctionPlugin plugin) {
        this.plugin = plugin;
        scheduler = this.plugin.getServer().getScheduler();
    }

    public String pickRandomStringFromList(List<String> list) {
		return list.get(ThreadLocalRandom.current().nextInt(0, list.size()));
    }
    
    private UUID checkOverLapHomes(Location pos, Player player) {
        UUID ownerUUID = null;
		for (UUID k: this.plugin.homes.keySet()) {
            Location home = this.plugin.homes.get(k);
			if (Math.abs(home.getBlockX() - pos.getBlockX()) <= 25) {
                if (Math.abs(home.getBlockZ() - pos.getBlockZ()) <= 25) {
                    ownerUUID = k;
                    break;
                }
			}
		}
        
        if (ownerUUID == null) {
            return null;
        }
        if (player == null) {
            return ownerUUID;
        }

        if (ownerUUID.equals(player.getUniqueId())) {
            return null;
        }
        if (this.plugin.allowList.containsKey(ownerUUID)) {
            if (this.plugin.allowList.get(ownerUUID).contains(player.getUniqueId())) {
                return null;
            }
        }

        return ownerUUID;
    }
    
    // private void logInfo(String msg) {
    //     this.plugin.getLogger().info(msg);
    // }

    @EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
        String name = player.getDisplayName();
        this.plugin.uuid2Names.put(player.getUniqueId(), name);
		
		if (this.plugin.nicknames.containsKey(player.getUniqueId())) {
			this.plugin.setNickname(player, this.plugin.nicknames.get(player.getUniqueId()));
			name = name + " (%s)".replace("%s", this.plugin.nicknames.get(player.getUniqueId()));
		}
		
		String greeting = ChatColor.GREEN + "----- 歡迎來到 MiMe Bro 伺服器 -----\n";
		greeting += ChatColor.RED + "1. 不准偷竊\n";
		greeting += ChatColor.RED + "2. 不准破壞別人家\n";
		greeting += ChatColor.RED + "3. 不准作弊開外掛\n";
		greeting += ChatColor.RED + "4. 暱稱是讓人好叫你，不是讓你隨便亂改\n";
		greeting += ChatColor.AQUA + "管理員: 幻墨 (Magic_lnk) 落心 (LShin0414)\n";
				greeting += ChatColor.AQUA + "      星洛 (XavierLves) GaGa (GAGA0927)\n";
		greeting += ChatColor.AQUA + "* 任何問題請問管理員，例如: 東西被偷、房子被破壞";
		greeting += ChatColor.GREEN + "------------------------------";
		
		player.sendMessage(greeting);

		event.setJoinMessage(ChatColor.YELLOW + this.pickRandomStringFromList(this.plugin.greetings).replace("%s", name));
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		String name = player.getDisplayName();
		
		if (this.plugin.nicknames.containsKey(player.getUniqueId())) {
			name = name + " (%s)".replace("%s", this.plugin.nicknames.get(player.getUniqueId()));
		}
		
		String goodbye = ChatColor.YELLOW + pickRandomStringFromList(this.plugin.goodbyes).replace("%s", name);
		event.setQuitMessage(goodbye);
	}

    @EventHandler
	public void onPlaceBlock(BlockPlaceEvent event) {
        UUID ownerUUID = checkOverLapHomes(event.getBlock().getLocation(), event.getPlayer());
        if (ownerUUID != null) {
            if (event.getPlayer().isOp()) {
                return;
            }

            event.setCancelled(true);

            if (this.plugin.uuid2Names.containsKey(ownerUUID)) {
                event.getPlayer().sendMessage(ChatColor.RED + "現在在 " + this.plugin.uuid2Names.get(ownerUUID) + " 家，不能建造");
			}
			else {
				event.getPlayer().sendMessage(ChatColor.RED + "現在其他人家，不能建造");
			}
			
        }
    }

    @EventHandler
	public void onBreakBlock(BlockBreakEvent event) {
        UUID ownerUUID = checkOverLapHomes(event.getBlock().getLocation(), event.getPlayer());
        if (ownerUUID != null) {
            if (event.getPlayer().isOp()) {
                return;
            }

            event.setCancelled(true);

            if (this.plugin.uuid2Names.containsKey(ownerUUID)) {
                event.getPlayer().sendMessage(ChatColor.RED + "現在在 " + this.plugin.uuid2Names.get(ownerUUID) + " 家，不能破壞");
			}
			else {
                event.getPlayer().sendMessage(ChatColor.RED + "現在其他人家，不能破壞");
            }
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        for (Block block: event.blockList()) {
            if (checkOverLapHomes(block.getLocation(), null) != null) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        for (Block block: event.blockList()) {
            if (checkOverLapHomes(block.getLocation(), null) != null) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority=EventPriority.HIGHEST)
	public void onOpenInventory(InventoryOpenEvent event) {
		if (event.getInventory().getHolder() != null) {
            UUID ownerUUID = checkOverLapHomes(event.getInventory().getLocation(), (Player) event.getPlayer());
            if (ownerUUID != null) {
                if (event.getPlayer().isOp()) {
                    return;
                }

                event.setCancelled(true);

                if (this.plugin.uuid2Names.containsKey(ownerUUID)) {
                    event.getPlayer().sendMessage(ChatColor.RED + "不能打開 " + this.plugin.uuid2Names.get(ownerUUID) + " 的箱子");
                }
                else {
                    event.getPlayer().sendMessage(ChatColor.RED + "不能打開其他人家的箱子");
                }
            }
        }
    }

    @EventHandler
	public void onPlayerSleep(PlayerBedEnterEvent event) {
		Player player = event.getPlayer();
        this.plugin.getServer().broadcastMessage(ChatColor.AQUA + player.getDisplayName() + " 在睡覺中");
        
        this.playersInSleep +=1;
        if (this.morningTask == null) {
            this.morningTask = this.scheduler.runTaskLater(this.plugin, new MyRunnable(this.plugin, this), 100);
        }
	}
	
	@EventHandler
	public void onPlayerWake(PlayerBedLeaveEvent event) {
		if (this.morningTask != null) {
			this.playersInSleep -= 1;
			if (this.playersInSleep == 0) {
				this.morningTask.cancel();
				this.morningTask = null;
			}
		}
	}
}