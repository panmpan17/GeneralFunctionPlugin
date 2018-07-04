package com.gmail.panmpan.GeneralFunctionPlugin;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
//import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
//import org.bukkit.entity.Entity;
//import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
//import org.bukkit.event.entity.EntityDamageByEntityEvent;
//import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
//import org.bukkit.inventory.ItemStack;
//import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class GeneralFunctionPlugin extends JavaPlugin implements Listener {
	List<String> greetings = new ArrayList<String>(); 
	List<String> goodbyes = new ArrayList<String>();
	
	private FileConfiguration customConfig = null;
	private File customConfigFile = null;
	private HashMap<UUID,String> nicknames = new HashMap<UUID,String>(); 
//	int HEADDROP_CHANCE = 2;

//	private ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
//	private SkullMeta meta_skull = (SkullMeta) skull.getItemMeta();
	
	@Override
	public void onEnable() {
		parseConfigYml();
		
		getServer().getPluginManager().registerEvents(this, this);
	}
	
	@Override
	public void onDisable() {
		this.saveNicknamesConfig();
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof Player) {
			if (cmd.getName().equalsIgnoreCase("nick")) {
				if (args.length < 1) {
					sender.sendMessage(ChatColor.RED + "必須指定一個暱稱");
					return true;
				}
				
				Player player = (Player) sender;
				String nickname = args[0];
				this.nicknames.put(player.getUniqueId(), nickname);
				this.setNickname(player, nickname);
				sender.sendMessage(ChatColor.GREEN + "成功把暱稱改成 " + nickname);
				return true;
			}
		}
		
		if (cmd.getName().equalsIgnoreCase("lightning")) {
			if (args.length < 1) {
				sender.sendMessage("必須指定一個玩家");
				return true;
			}
			
			try {
				Player player = Bukkit.getPlayer(args[0]);
				player.getWorld().strikeLightning(player.getLocation());
				return true;
			}
			catch (Exception e) {
				sender.sendMessage("玩家不存在");
				return true;
			}
		}
		return false;
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		String name = player.getDisplayName();
		
		if (this.nicknames.containsKey(player.getUniqueId())) {
			this.setNickname(player, this.nicknames.get(player.getUniqueId()));
			name = name + " (%s)".replace("%s", this.nicknames.get(player.getUniqueId()));
		}
		
		String greeting = ChatColor.YELLOW + pickRandomStringFromList(greetings).replace("%s", name);
		event.setJoinMessage(greeting);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		String name = player.getDisplayName();
		
		if (this.nicknames.containsKey(player.getUniqueId())) {
			name = name + " (%s)".replace("%s", this.nicknames.get(player.getUniqueId()));
		}
		
		String goodbye = ChatColor.YELLOW + pickRandomStringFromList(goodbyes).replace("%s", name);
		event.setQuitMessage(goodbye);
	}
	
	public void parseConfigYml() {
		this.saveDefaultConfig();
		FileConfiguration config = this.getConfig();
		
		List<String> config_greetings = config.getStringList("greeting");
		List<String> config_goodbyes = config.getStringList("goodbye");
		if (config_greetings.size() >= 1) {
			greetings = config_greetings;
		}
		else {
			greetings.add("歡迎, %s");
		}
		if (config_goodbyes.size() >= 1) {
			goodbyes = config_goodbyes;
		}
		else {
			goodbyes.add("拜拜, %s");
		}
		
		this.saveDefaultNicknamesConfig();
		this.customConfig = YamlConfiguration.loadConfiguration(this.customConfigFile);
		ConfigurationSection nicknamesConfig = this.customConfig.getConfigurationSection("nicknames");
		
		for(String key : nicknamesConfig.getKeys(true)){
			UUID playerUUID = UUID.fromString(key);
			String nickname = nicknamesConfig.getString(key);
			this.nicknames.put(playerUUID, nickname);
		}
	}
	
	private void setNickname(Player player, String nickname) {
		player.setDisplayName(nickname);
		player.setPlayerListName(nickname);
		player.setCustomName(nickname);
		player.setCustomNameVisible(true);
	}
	
	public String pickRandomStringFromList(List<String> list) {
		return list.get(ThreadLocalRandom.current().nextInt(0, list.size()));
	}
	
	private void saveDefaultNicknamesConfig() {
	    if (this.customConfigFile == null) {
	        this.customConfigFile = new File(getDataFolder(), "nicknames.yml");
	    }
	    if (!this.customConfigFile.exists()) {
	    	this.saveResource("nicknames.yml", false);
	    }
	}
	
	private void saveNicknamesConfig() {
		if (this.customConfig == null || this.customConfigFile == null) {
	        return;
	    }

	    try {
	    	for (UUID playerUUID: this.nicknames.keySet()) {
	    		this.customConfig.set("nicknames." + String.valueOf(playerUUID), this.nicknames.get(playerUUID));
	    	}
	    	
	        this.customConfig.save(this.customConfigFile);
	    } catch (IOException ex) {}
	}
	
//	@EventHandler
//	public void onEntityDeath(EntityDeathEvent event) {
//		if (event.getEntity().getType() == EntityType.PLAYER) {
//			Player killer = event.getEntity().getKiller();
//			if (killer != null) {
//				float chance = ThreadLocalRandom.current().nextInt(1, (int) HEADDROP_CHANCE + 1);
//
//				getLogger().info(String.valueOf(chance));
//				getLogger().info(String.valueOf(HEADDROP_CHANCE));
//				if (chance == HEADDROP_CHANCE) {
//					meta_skull.setOwner(event.getEntity().getName());
//					skull.setItemMeta(meta_skull);
//					
//					killer.getInventory().addItem(skull);
//					
//				}
//			}
//		}
//	}
}
