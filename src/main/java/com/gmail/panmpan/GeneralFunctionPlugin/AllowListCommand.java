package com.gmail.panmpan.GeneralFunctionPlugin;

import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class AllowListCommand implements CommandExecutor {
    final private GeneralFunctionPlugin plugin;

    public AllowListCommand(GeneralFunctionPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return false;
        }

        Player player = (Player) sender;
        UUID playerUUID = player.getUniqueId();
        if (args.length == 0) {
            String help = ChatColor.AQUA + "/allow-list add <玩家>" + ChatColor.WHITE + " 允許玩家在你家破壞、建造、開箱\n";
            help += ChatColor.AQUA + "/allow-list remove <玩家>" + ChatColor.WHITE + " 把玩家從允許名單中移除\n";
            help += ChatColor.AQUA + "/allow-list removei <名單順序>" + ChatColor.WHITE + " 和 remove 一樣功能\n";
            help += ChatColor.GRAY + "＊ 適用於離線玩家，查詢名單順序請打 /allow-list list\n";
            help += ChatColor.AQUA + "/allow-list list" + ChatColor.WHITE + " 列出所有允許的玩家";
            sender.sendMessage(help);
        }
        else if (args[0].equalsIgnoreCase("add")) {
            if (args.length == 1) {
                sender.sendMessage(ChatColor.RED + "需要指定一名玩家");
                return true;
            }

            Player targetPlayer = this.plugin.getServer().getPlayer(args[1]);
            if (targetPlayer == null) {
                sender.sendMessage(ChatColor.RED + "玩家不在線或不存在");
                return true;
            }
            if (targetPlayer.getUniqueId().equals(playerUUID)) {
                sender.sendMessage(ChatColor.RED + "不能指定自己");
                return true;
            }

            if (!this.plugin.allowList.containsKey(playerUUID)) {
                this.plugin.allowList.put(playerUUID, new ArrayList<UUID>());
            }

            if (this.plugin.allowList.get(playerUUID).contains(targetPlayer.getUniqueId())) {
                sender.sendMessage(ChatColor.RED + "玩家已存在");
                return true;
            }

            this.plugin.allowList.get(playerUUID).add(targetPlayer.getUniqueId());
            sender.sendMessage(ChatColor.GREEN + "玩家加入名單");
        }
        else if (args[0].equalsIgnoreCase("remove")) {
            if (args.length == 1) {
                sender.sendMessage(ChatColor.RED + "需要指定一名玩家");
                return true;
            }

            Player targetPlayer = this.plugin.getServer().getPlayer(args[1]);
            if (targetPlayer == null) {
                sender.sendMessage(ChatColor.RED + "玩家不在線或不存在");
                return true;
            }
            if (targetPlayer.getUniqueId().equals(playerUUID)) {
                sender.sendMessage(ChatColor.RED + "不能指定自己");
                return true;
            }

            if (!this.plugin.allowList.containsKey(playerUUID)) {
                this.plugin.allowList.put(playerUUID, new ArrayList<UUID>());
                sender.sendMessage(ChatColor.RED + "玩家不在名單中");
                return true;
            }

            if (this.plugin.allowList.get(playerUUID).contains(targetPlayer.getUniqueId())) {
                sender.sendMessage(ChatColor.RED + "玩家不在名單中");
                return true;
            }

            this.plugin.allowList.get(playerUUID).remove(targetPlayer.getUniqueId());
            sender.sendMessage(ChatColor.GREEN + "玩家從名單移除");
        }
        else if (args[0].equalsIgnoreCase("removei")) {
            if (args.length == 1) {
                sender.sendMessage(ChatColor.RED + "需要指定一個名單順序");
                return true;
            }

            Integer index;
            try {
                index = Integer.parseInt(args[1]);
            } catch (Exception e) {
                //TODO: handle exception
                sender.sendMessage(ChatColor.RED + "必須是一個數字");
                return true;
            }

            if (this.plugin.allowList.containsKey(playerUUID)) {
                this.plugin.allowList.put(playerUUID, new ArrayList<UUID>());
                sender.sendMessage(ChatColor.RED + "這數字超過名單長度");
                return true;
            }
            if (index < this.plugin.allowList.get(playerUUID).size()) {
                sender.sendMessage(ChatColor.RED + "這數字超過名單長度");
                return true;
            }

            this.plugin.allowList.get(playerUUID).remove(index);
            sender.sendMessage(ChatColor.GREEN + "玩家從名單移除");
        }
        else if (args[0].equalsIgnoreCase("list")) {
            String list = ChatColor.GOLD + "以下是你名單中的玩家:\n";

            Integer index = 0;
            for (UUID targetUUID: this.plugin.allowList.get(playerUUID)) {
                if (this.plugin.uuid2Names.containsKey(targetUUID)) {
                    list += ChatColor.BLUE + String.valueOf(index) + ChatColor.WHITE + " " + this.plugin.uuid2Names.get(targetUUID) + "\n";
                }
                else {
                    list += ChatColor.BLUE + String.valueOf(index) + ChatColor.WHITE + " " + targetUUID.toString() + ChatColor.GRAY + " 找不到這名玩家的名字\n";
                }
            }

            list += ChatColor.GRAY + "\n可以利用 https://mcuuid.net 找尋找不到名字的玩家";
            player.sendMessage(list);
        }
		return true;
	}
}