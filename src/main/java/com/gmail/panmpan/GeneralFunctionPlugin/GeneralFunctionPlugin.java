package com.gmail.panmpan.GeneralFunctionPlugin;

import java.util.List;
import java.util.UUID;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

// import org.bukkit.Bukkit;
// import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
// import org.bukkit.command.Command;
// import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class GeneralFunctionPlugin extends JavaPlugin {
	public List<String> greetings = new ArrayList<String>(); 
	public List<String> goodbyes = new ArrayList<String>();
	
	private FileConfiguration customConfig = null;
	private File customConfigFile = null;
	public HashMap<UUID,String> nicknames = new HashMap<UUID,String>(); 
	public HashMap<UUID,Location> homes = new HashMap<UUID,Location>();
	// private HashMap<UUID, List<UUID>> breakWhitelist = new HashMap<UUID, List<UUID>>();	
	
	@Override
	public void onEnable() {
		parseConfigYml();
		
		getServer().getPluginManager().registerEvents(new MyListener(this), this);
		this.getCommand("home").setExecutor(new HomeCommand(this));
		this.getCommand("nick").setExecutor(new NickCommand(this));
		this.getCommand("lightning").setExecutor(new LightningCommand(this));
	}
	
	@Override
	public void onDisable() {
		this.saveCustomConfig();
	}
	
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
	
	public void setNickname(Player player, String nickname) {
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
	        this.customConfigFile = new File(getDataFolder(), "datas.yml");
	    }
	    if (!this.customConfigFile.exists()) {
	    	this.saveResource("datas.yml", false);
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
}
