package io.github.netdex.CircuitDetector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import io.github.netdex.CircuitDetector.listeners.ExistenceTask;
import io.github.netdex.CircuitDetector.listeners.RedstoneUpdateListener;
import io.github.netdex.CircuitDetector.listeners.RefreshTask;
import io.github.netdex.CircuitDetector.util.Util;
import io.github.netdex.CircuitDetector.util.Violation;
import net.gravitydevelopment.updater.Updater;
import net.gravitydevelopment.updater.Updater.UpdateResult;

public class CircuitDetector extends JavaPlugin implements Listener {
	
	public static FileConfiguration CONFIG;
	
	public static HashMap<UUID, Boolean> LOGGING = new HashMap<UUID, Boolean>();
	public static ArrayList<Violation> VIOLATIONS = new ArrayList<Violation>();
	
	public int THRESHOLD = 0;
	public int REFRESH_TIME = 60;

	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);
		getServer().getPluginManager().registerEvents(new RedstoneUpdateListener(this), this);
		
		CONFIG = getConfig();

		if (CONFIG.get("threshold") != null) {
			THRESHOLD = CONFIG.getInt("threshold");
		}
		if (CONFIG.get("refreshTime") != null) {
			REFRESH_TIME = CONFIG.getInt("refreshTime");
		}
		
		// Handle commands in another class
		this.getCommand("cd").setExecutor(new CommandManager(this));

		// Set timers
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
		scheduler.scheduleSyncRepeatingTask(this, new RefreshTask(), 0L, REFRESH_TIME * 20L);
		scheduler.scheduleSyncRepeatingTask(this, new ExistenceTask(this), 0L, 5L);
		
		Updater updater = new Updater(this, 84429, this.getFile(), Updater.UpdateType.DEFAULT, true);
		if(updater.getResult() == UpdateResult.SUCCESS){
			for(Player p : getServer().getOnlinePlayers()){
				if(p.hasPermission("CircuitDetector.cd")){
					Util.sendMessage(p, "Circuit Detector has updated to version \"" + updater.getLatestName() + "\"! Reload the server to load the new update.");
				}
			}
		}
	}

	public void onDisable() {
		// Cancel the timers
		Bukkit.getServer().getScheduler().cancelAllTasks();
		// Save configuration
		CONFIG.set("threshold", THRESHOLD);
		CONFIG.set("refreshTime", REFRESH_TIME);
		saveConfig();
	}
	
	public static Violation getViolation(Location loc){
		for(Violation v : VIOLATIONS){
			if(v.getLocation().equals(loc))
				return v;
			
		}
		return null;
	}

}
