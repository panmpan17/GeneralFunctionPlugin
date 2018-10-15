package com.gmail.panmpan.GeneralFunctionPlugin;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.World.Environment;

public class MyRunnable implements Runnable {
    final GeneralFunctionPlugin plugin;
    final MyListener listener;

    public MyRunnable(GeneralFunctionPlugin plugin, MyListener listener){
        this.plugin = plugin;
        this.listener = listener;
    }

    @Override
    public void run () {
        plugin.getServer().broadcastMessage(ChatColor.AQUA + "早安啊 !");
        
        for (World world: this.plugin.getServer().getWorlds()) {
            if (world.getEnvironment() == Environment.NORMAL) {
                world.setTime(23459);

                if (world.hasStorm()) {
                    world.setStorm(false);
                }
                break;
            }
        }

        this.listener.morningTask = null;
        this.listener.playersInSleep = 0;
    }
}