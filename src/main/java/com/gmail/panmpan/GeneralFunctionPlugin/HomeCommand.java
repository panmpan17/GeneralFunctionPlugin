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
		else if (args[0].equalsIgnoreCase("s") || args[0].equalsIgnoreCase("set")) {
			this.SetHome(sender);
			return true;
		}
		else if (args[0].equalsIgnoreCase("v") || args[0].equalsIgnoreCase("visit")) {
			if (args.length < 2) {
				sender.sendMessage(ChatColor.RED + "請指定要拜訪的玩家");
				return true;
			}

			this.VisitHome(sender, args[1]);
			return true;
		}
		else if (args[0].equalsIgnoreCase("c") || args[0].equalsIgnoreCase("check")) {
			this.checkHome(sender);
			return true;
		}
		else if (args[0].equalsIgnoreCase("help")) {
			String help = ChatColor.GOLD + "家功能 1.快速傳送 2.玩家拜訪 3.領地保護 (更多資訊在 /allow-list)\n\n";
            help += ChatColor.AQUA + "/home" + ChatColor.WHITE + " 傳送到家裡\n";
            help += ChatColor.AQUA + "/home s[set]" + ChatColor.WHITE + " 設定家的位置\n";
            help += ChatColor.AQUA + "/home v[visit] <玩家>" + ChatColor.WHITE + " 拜訪別人家裡\n";
            help += ChatColor.AQUA + "/home c[check]" + ChatColor.WHITE + " 檢查是否在家裡\n";
			sender.sendMessage(help);
			
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
			if (this.plugin.uuid2Names.containsKey(ownerUUID)) {
				player.sendMessage(ChatColor.RED + "離 " + this.plugin.uuid2Names.get(ownerUUID) + " 家太近");
			}
			else {
				player.sendMessage(ChatColor.RED + "離其他人家太近");
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
			targetPlayer.sendMessage(ChatColor.AQUA + player.getDisplayName() + " 來拜訪妳了");
			return;
		}
	}
	
	private void checkHome(CommandSender sender) {
		Player player = (Player) sender;

		if (this.plugin.homes.containsKey(player.getUniqueId())) {
			Location home = this.plugin.homes.get(player.getUniqueId());

			if (Math.abs(home.getBlockX() - player.getLocation().getBlockX()) <= 25) {
                if (Math.abs(home.getBlockZ() - player.getLocation().getBlockZ()) <= 25) {
					player.sendMessage(ChatColor.AQUA + "在你家");
					return;
				}
			}
			player.sendMessage(ChatColor.LIGHT_PURPLE + "不在你家");
			return;
		}
		player.sendMessage(ChatColor.RED + "你還沒有家");
	}
}