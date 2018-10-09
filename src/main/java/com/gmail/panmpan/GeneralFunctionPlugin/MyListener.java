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
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class MyListener implements Listener {
    private final GeneralFunctionPlugin plugin;

    public MyListener(GeneralFunctionPlugin plugin) {
        this.plugin = plugin;
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

        return ownerUUID;
    }
    
    // private void logInfo(String msg) {
    //     this.plugin.getLogger().info(msg);
    // }

    @EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerJoin(PlayerJoinEvent event) {
        this.plugin.getLogger().info("Player Join Event");

		Player player = event.getPlayer();
		String name = player.getDisplayName();
		
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

            Player owner = this.plugin.getServer().getPlayer(ownerUUID);
            
            if (owner == null) {
				event.getPlayer().sendMessage(ChatColor.RED + "現在其他人家，不能建造");
			}
			else {
				event.getPlayer().sendMessage(ChatColor.RED + "現在在 " + owner.getDisplayName() + " 家，不能建造");
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

            Player owner = this.plugin.getServer().getPlayer(ownerUUID);

            if (owner == null) {
				event.getPlayer().sendMessage(ChatColor.RED + "現在其他人家，不能破壞");
			}
			else {
                event.getPlayer().sendMessage(ChatColor.RED + "現在在 " + owner.getDisplayName() + " 家，不能破壞");
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
}