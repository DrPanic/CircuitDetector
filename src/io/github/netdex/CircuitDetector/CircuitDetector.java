package io.github.netdex.CircuitDetector;

import io.github.netdex.CircuitDetector.listeners.ExistenceTask;
import io.github.netdex.CircuitDetector.listeners.RedstoneUpdateListener;
import io.github.netdex.CircuitDetector.listeners.RefreshTask;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

public class CircuitDetector extends JavaPlugin implements Listener {
	private FileConfiguration config;
	
	public HashMap<UUID, Boolean> playersLogging = new HashMap<UUID, Boolean>();
	public HashMap<Location, Integer> violations = new HashMap<Location, Integer>();
	
	public int THRESHOLD = 0;
	public int REFRESH_TIME = 60;

	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);
		getServer().getPluginManager().registerEvents(new RedstoneUpdateListener(this, violations), this);
		
		config = getConfig();

		if (config.get("threshold") != null) {
			THRESHOLD = config.getInt("threshold");
		}
		if (config.get("refreshTime") != null) {
			REFRESH_TIME = config.getInt("refreshTime");
		}
		
		// Handle commands in another class
		this.getCommand("cd").setExecutor(new CommandManager(this));

		// Set timers
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
		scheduler.scheduleSyncRepeatingTask(this, new RefreshTask(violations), 0L, REFRESH_TIME * 20L);
		scheduler.scheduleSyncRepeatingTask(this, new ExistenceTask(this), 0L, 5L);
	}

	public void onDisable() {
		// Cancel the timers
		Bukkit.getServer().getScheduler().cancelAllTasks();
		// Save configuration
		config.set("threshold", THRESHOLD);
		config.set("refreshTime", REFRESH_TIME);
		saveConfig();
	}

}
