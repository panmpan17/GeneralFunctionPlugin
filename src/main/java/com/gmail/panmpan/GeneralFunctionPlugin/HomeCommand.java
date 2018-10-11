package com.gmail.panmpan.GeneralFunctionPlugin;

import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HomeCommand implements CommandExecutor {
    final private GeneralFunctionPlugin plugin;

    public HomeCommand (GeneralFunctionPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return false;
		}
		
		if (args.length == 0) {
			this.TeleportHome(sender);
			return true;
		}
		else if (args[0].equalsIgnoreCase("s")) {
			this.SetHome(sender);
			return true;
		}
		else if (args[0].equalsIgnoreCase("v")) {
			if (args.length < 2) {
				sender.sendMessage(ChatColor.RED + "請指定要拜訪的玩家");
				return true;
			}

			this.VisitHome(sender, args[1]);
			return true;
		}
        return false;
    }

    private void TeleportHome(CommandSender sender) {
		Player player = (Player) sender;
		
		if (this.plugin.homes.containsKey(player.getUniqueId())) {
			player.teleport(this.plugin.homes.get(player.getUniqueId()));
			player.sendMessage(ChatColor.GOLD + "歡迎回家!");
		}
		else {
			player.sendMessage(ChatColor.RED + "尚未設定家的位置");
		}
	}

    private void SetHome(CommandSender sender) {
		Player player = (Player) sender;
		Location location = player.getLocation();

		UUID ownerUUID = null;

		for (UUID k: this.plugin.homes.keySet()) {
            Location home = this.plugin.homes.get(k);
			if (Math.abs(home.getBlockX() - location.getBlockX()) <= 25) {
                if (Math.abs(home.getBlockZ() - location.getBlockZ()) <= 25) {
                    ownerUUID = k;
                    break;
                }
			}
		}

		if (ownerUUID == null || ownerUUID.equals(player.getUniqueId())) {
			this.plugin.homes.put(player.getUniqueId(), location);
			player.sendMessage(ChatColor.GREEN + "成功把家設在 " + this.plugin.stringfyLocation(location, true));
		}
		else {
			Player owner = this.plugin.getServer().getPlayer(ownerUUID);

			if (owner == null) {
				player.sendMessage(ChatColor.RED + "離其他玩家家太近");
			}
			else {
				player.sendMessage(ChatColor.RED + "離 " + owner.getDisplayName() + " 家太近");
			}
		}
    }
    
    private void VisitHome(CommandSender sender, String playerName) {
		Player player = (Player) sender;
		Player targetPlayer = this.plugin.getServer().getPlayer(playerName);
		
		if (targetPlayer == null) {
			player.sendMessage(ChatColor.RED + "玩家不在線不能拜訪");
			return;
		}
		
		if (this.plugin.homes.containsKey(targetPlayer.getUniqueId())) {
			player.teleport(this.plugin.homes.get(targetPlayer.getUniqueId()));
			player.sendMessage(ChatColor.GOLD + "歡迎來到 " + targetPlayer.getDisplayName() + " 的家");
			return;
		}
    }
}