package com.afforess.minecartmaniastation;

import org.bukkit.Server;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import com.afforess.minecartmaniacore.MinecartManiaCore;
import com.afforess.minecartmaniacore.config.MinecartManiaConfigurationParser;
import com.afforess.minecartmaniacore.debug.MinecartManiaLogger;

public class MinecartManiaStation extends JavaPlugin {
    public static MinecartManiaLogger log = MinecartManiaLogger.getInstance();
    public static Server server;
    public static PluginDescriptionFile description;
    public static MinecartActionListener listener = new MinecartActionListener();
    
    public void onDisable() {
        // TODO Auto-generated method stub
        
    }
    
    public void onEnable() {
        server = getServer();
        description = getDescription();
        MinecartManiaConfigurationParser.read(description.getName() + "Configuration.xml", MinecartManiaCore.getDataDirectoryRelativePath(), new StationSettingParser());
        getServer().getPluginManager().registerEvents(listener, this);
        //        getServer().getPluginManager().registerEvent(Event.Type.CUSTOM_EVENT, listener, Priority.Normal, this);
        log.info(description.getName() + " version " + description.getVersion() + " is enabled!");
    }
}
