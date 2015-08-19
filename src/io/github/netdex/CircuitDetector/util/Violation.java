package io.github.netdex.CircuitDetector.util;

import static org.bukkit.ChatColor.*;

import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;

import mkremins.fanciful.FancyMessage;

public class Violation {

	private Location loc;
	private long lastInstance;
	private long timeDiff;
	private int instances;

	public Violation(Location loc) {
		this.loc = loc;
		instances = 0;
	}

	/**
	 * Increase the instance counter by 1
	 */
	public void addInstance() {
		instances++;
	}

	/**
	 * Get the number of instances of this violation
	 * 
	 * @return the number of instances of this violation
	 */
	public int getInstances() {
		return instances;
	}

	public void updateCTime(){
		timeDiff = System.currentTimeMillis() - lastInstance;
		lastInstance = System.currentTimeMillis();
	}
	
	/**
	 * Get the difference in time from given to last activation
	 * 
	 * @return the difference in time from given to last activation
	 */
	public long getTimeDiff() {
		return timeDiff;
	}

	/**
	 * Get the violation's location
	 * 
	 * @return the violation's location
	 */
	public Location getLocation() {
		return loc;
	}

	/**
	 * Get the violation's chunk
	 * 
	 * @return the violation's chunk
	 */
	public Chunk getChunk() {
		return loc.getChunk();
	}

	/**
	 * Destroy the violation's circuit
	 */
	public void nukeCircuit() {
		Util.destroyCircuit(loc.getBlock(), true);
	}

	public FancyMessage getLogMessage() {
		return new FancyMessage("[TP] ")
					.color(GREEN).style(BOLD)
					.tooltip("Teleport to " + Util.formatLocation(loc))
					.command("/tp " + loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ())
				.then("[KILL]")
					.color(RED).style(BOLD)
					.tooltip("Kill entire circuit")
					.command("/cd kill " + loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ())
				
				.then(" : ")
				.color(DARK_GRAY)
				.then("\"").color(AQUA)
				.then(loc.getBlock().getType().name()).color(AQUA)
				.then("\"").color(AQUA)
				.then(" x").color(DARK_RED)
				.then(instances + "").color(DARK_RED)
				.then(" DIFF: ").color(ChatColor.GREEN)
				.then(timeDiff + "");
		
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof Violation) {
			if (((Violation) o).getLocation() == loc)
				return true;
		}
		return false;
	}
}
