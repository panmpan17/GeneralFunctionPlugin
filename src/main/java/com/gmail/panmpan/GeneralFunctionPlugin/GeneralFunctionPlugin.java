package com.gmail.panmpan.GeneralFunctionPlugin;

import java.util.List;
import java.util.UUID;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
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
	private HashMap<UUID,Location> homes = new HashMap<UUID,Location>();
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
		this.saveCustomConfig();
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof Player) {
			if (cmd.getName().equalsIgnoreCase("nick")) {
				this.handleNickCommand(sender, args);
				return true;
			}
			else if (cmd.getName().equalsIgnoreCase("sethome")) {
				this.handleSethomeCommand(sender);
				return true;
			}
			else if (cmd.getName().equalsIgnoreCase("home")) {
				this.handleHomeCommand(sender);
				return true;
			}
//			else if (cmd.getName().equalsIgnoreCase("visit")) {
//				this.handleVisitCommand(sender, args);
//			}
		}
		if (cmd.getName().equalsIgnoreCase("lightning")) {
			this.handleLightningCommand(sender, args);
			return true;
		}
		return false;
	}
	
	private void handleLightningCommand(CommandSender sender, String[] args) {
		if (args.length < 1) {
			sender.sendMessage(ChatColor.RED + "必須指定一個玩家");
			return;
		}
		
		try {
			Player player = Bukkit.getPlayer(args[0]);
			player.getWorld().strikeLightning(player.getLocation());
		}
		catch (Exception e) {
			sender.sendMessage(ChatColor.RED + "玩家不存在");
			return;
		}
	}
	
	private void handleNickCommand(CommandSender sender, String[] args) {
		if (args.length < 1) {
			sender.sendMessage(ChatColor.RED + "必須指定一個暱稱");
			return;
		}
		
		Player player = (Player) sender;
		String nickname = args[0];
		this.nicknames.put(player.getUniqueId(), nickname);
		this.setNickname(player, nickname);
		sender.sendMessage(ChatColor.GREEN + "成功把暱稱改成 " + nickname);
	}
	
	private void handleSethomeCommand(CommandSender sender) {
		Player player = (Player) sender;
		Location location = player.getLocation();
		this.homes.put(player.getUniqueId(), location);
		
		player.sendMessage(ChatColor.GREEN + "成功把家設在 " + stringfyLocation(location, true));
	}
	
	private void handleHomeCommand(CommandSender sender) {
		Player player = (Player) sender;
		
		if (this.homes.containsKey(player.getUniqueId())) {
			player.teleport(this.homes.get(player.getUniqueId()));
			player.sendMessage(ChatColor.GOLD + "歡迎回家!");
		}
		else {
			player.sendMessage(ChatColor.RED + "尚未設定家的位置");
		}
	}
	
//	@SuppressWarnings("deprecation")
//	private void handleVisitCommand(CommandSender sender, String[] args) {
//		if (args.length < 1) {
//			sender.sendMessage(ChatColor.RED + "必須指定一個玩家");
//			return;
//		}
//		
//		Player player = (Player) sender;
//		Player targetPlayer = null;
//		try {
//			targetPlayer = Bukkit.getPlayer(args[0]);
//		}
//		catch (Exception e) {
//			player.sendMessage("玩家不存在");
//			return;
//		}
//		
//		if (this.homes.containsKey(targetPlayer.getUniqueId())) {
//			player.teleport(this.homes.get(targetPlayer.getUniqueId()));
//			player.sendMessage(ChatColor.GOLD + "歡迎來到 " + targetPlayer.getDisplayName() + " 的家");
//			return;
//		}
//	}
	
	public String stringfyLocation(Location location, boolean simplefy) {
		String world = location.getWorld().getName();
		if (simplefy) {
			String x = String.valueOf((int) location.getX());
			String y = String.valueOf((int) location.getY());
			String z = String.valueOf((int) location.getZ());
			return  x + "," + y + "," + z;
		}
		
		String x = String.valueOf((float) location.getX());
		String y = String.valueOf((float) location.getY());
		String z = String.valueOf((float) location.getZ());
		String yaw = String.valueOf((float) location.getYaw());
		String pitch = String.valueOf((float) location.getPitch());
		
		return world + "," + x + "," + y + "," + z + "," + yaw + "," + pitch;
	}
	
	public Location parseLocation(String locationString) {
		String[] list = locationString.split(",");
		World world = getServer().getWorld(list[0]);
		float x = Float.valueOf(list[1]);
		float y = Float.valueOf(list[2]);
		float z = Float.valueOf(list[3]);
		float yaw = Float.valueOf(list[4]);
		float pitch = Float.valueOf(list[5]);
		
		return new Location(world, x, y, z, yaw, pitch);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		String name = player.getDisplayName();
		
		if (this.nicknames.containsKey(player.getUniqueId())) {
			this.setNickname(player, this.nicknames.get(player.getUniqueId()));
			name = name + " (%s)".replace("%s", this.nicknames.get(player.getUniqueId()));
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

		event.setJoinMessage(ChatColor.YELLOW + pickRandomStringFromList(greetings).replace("%s", name));
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
		
		this.saveDefaultCustomConfigFile();

		this.customConfig = YamlConfiguration.loadConfiguration(this.customConfigFile);
		ConfigurationSection nicknamesConfig = this.customConfig.getConfigurationSection("nicknames");
		ConfigurationSection homesConfig = this.customConfig.getConfigurationSection("homes");
		
		for(String key: nicknamesConfig.getKeys(true)){
			UUID playerUUID = UUID.fromString(key);
			String nickname = nicknamesConfig.getString(key);
			this.nicknames.put(playerUUID, nickname);
		}
		
		for (String key: homesConfig.getKeys(true)) {
			UUID playerUUID = UUID.fromString(key);
			String locationString = homesConfig.getString(key);
			this.homes.put(playerUUID, parseLocation(locationString));
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
	
	private void saveDefaultCustomConfigFile() {
	    if (this.customConfigFile == null) {
	        this.customConfigFile = new File(getDataFolder(), "nicknames.yml");
	    }
	    if (!this.customConfigFile.exists()) {
	    	this.saveResource("nicknames.yml", false);
	    }
	}
	
	private void saveCustomConfig() {
		if (this.customConfig == null || this.customConfigFile == null) {
	        return;
	    }

	    try {
	    	for (UUID playerUUID: this.nicknames.keySet()) {
	    		this.customConfig.set("nicknames." + String.valueOf(playerUUID), this.nicknames.get(playerUUID));
	    	}
	    	for (UUID playerUUID: this.homes.keySet()) {
	    		this.customConfig.set("homes." + String.valueOf(playerUUID), stringfyLocation(this.homes.get(playerUUID), false));
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
