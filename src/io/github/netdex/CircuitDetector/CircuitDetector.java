package io.github.netdex.CircuitDetector;

import io.github.netdex.CircuitDetector.listeners.ExistenceListener;
import io.github.netdex.CircuitDetector.listeners.RedstoneUpdateListener;

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
	// Logging players
	public static HashMap<UUID, Boolean> LOGGING = new HashMap<UUID, Boolean>();
	// Violations
	public static HashMap<Location, Integer> VIOLATIONS = new HashMap<Location, Integer>();
	// Destruction threshold
	public static int THRESHOLD = 0;
	// Refresh grace time
	public static int REFRESH_TIME = 60;

	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);
		getServer().getPluginManager().registerEvents(new RedstoneUpdateListener(VIOLATIONS), this);
		
		config = getConfig();

		if (config.get("threshold") != null) {
			THRESHOLD = config.getInt("threshold");
		}
		if (config.get("refreshTime") != null) {
			REFRESH_TIME = config.getInt("refreshTime");
		}
		
		// Register the CommandExecutor so commands are handled by the other
		// class
		this.getCommand("cd").setExecutor(new CommandManager());

		// Clear all violations every 60 seconds by default, or however
		// configured
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
		scheduler.scheduleSyncRepeatingTask(this, new Runnable() {
			@Override
			public void run() {
				try {
					VIOLATIONS.clear();
				} catch (Exception e) {}
			}
		}, 0L, REFRESH_TIME * 20L);
		scheduler.scheduleSyncRepeatingTask(this, new ExistenceListener(this), 0L, 5L);
	}

	public void onDisable() {
		Bukkit.getServer().getScheduler().cancelAllTasks();
		config.set("threshold", THRESHOLD);
		config.set("refreshTime", REFRESH_TIME);
		saveConfig();

		VIOLATIONS = null;
		LOGGING = null;
	}

	/**
	 * Simple getter to save time
	 * 
	 * @return a hashmap containing the violations
	 */
	public HashMap<Location, Integer> getViolations() {
		return VIOLATIONS;
	}

	public static int getThreshold() {
		return THRESHOLD;
	}

}
