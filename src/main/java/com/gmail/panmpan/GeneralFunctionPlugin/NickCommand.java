package com.gmail.panmpan.GeneralFunctionPlugin;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class NickCommand implements CommandExecutor {
	private final GeneralFunctionPlugin plugin;

	public NickCommand(GeneralFunctionPlugin plugin) {
		this.plugin = plugin;
	}

    @Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
            return false;
        }
		if (args.length < 1) {
			sender.sendMessage(ChatColor.RED + "必須指定一個暱稱");
			return true;
		}

		Player player = (Player) sender;
		String nickname = args[0];
		this.plugin.nicknames.put(player.getUniqueId(), nickname);
		this.plugin.setNickname(player, nickname);
		sender.sendMessage(ChatColor.GREEN + "成功把暱稱改成 " + nickname);

		return true;
	}
}