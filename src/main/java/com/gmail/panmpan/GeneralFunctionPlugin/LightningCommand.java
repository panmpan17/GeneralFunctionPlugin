package com.gmail.panmpan.GeneralFunctionPlugin;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LightningCommand implements CommandExecutor {
    final private GeneralFunctionPlugin plugin;

    public LightningCommand(GeneralFunctionPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length < 1) {
			sender.sendMessage(ChatColor.RED + "必須指定一個玩家");
			return true;
		}
		
		try {
			Player player = this.plugin.getServer().getPlayer(args[0]);
			player.getWorld().strikeLightning(player.getLocation());
		}
		catch (Exception e) {
			sender.sendMessage(ChatColor.RED + "玩家不存在");
        }
        return true;
	}
}