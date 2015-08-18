package io.github.netdex.CircuitDetector.util;

import static org.bukkit.ChatColor.AQUA;
import static org.bukkit.ChatColor.BLUE;
import static org.bukkit.ChatColor.DARK_GRAY;
import static org.bukkit.ChatColor.DARK_RED;
import static org.bukkit.ChatColor.GRAY;
import static org.bukkit.ChatColor.ITALIC;
import static org.bukkit.ChatColor.UNDERLINE;

import org.bukkit.Location;

import mkremins.fanciful.FancyMessage;

public class Violation {
	
	private Location loc;
	private long lastInstance;
	private int instances;
	
	public Violation(Location loc, long time){
		this.loc = loc;
		this.lastInstance = time;
		instances = 0;
	}
	
	public void addInstance(){
		instances++;
	}
	public int getInstances(){
		return instances;
	}
	public long getTimeDiff(long ctime){
		return ctime - lastInstance;
	}
	
	public Location getLocation(){
		return loc;
	}
	public FancyMessage getLogMessage(){
		return new FancyMessage(Util.getDate())
				.color(BLUE)
			.then(" : ")
				.color(DARK_GRAY)
			.then("\"")
				.color(AQUA)
			.then(loc.getBlock().getType().name())
				.style(ITALIC)
			.then("\" at ")
				.color(AQUA)
			.then(Util.formatLocation(loc))
				.color(GRAY)
				.style(UNDERLINE)
				.tooltip("Click here to teleport")
				.command("/tp " + loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ())
			.then(" x")
				.color(DARK_RED)
			.then(instances + "")
				.color(DARK_RED);
	}
	@Override
	public boolean equals(Object o){
		if(o instanceof Violation){
			if(((Violation)o).getLocation() == loc)
				return true;
		}
		return false;
	}
}
