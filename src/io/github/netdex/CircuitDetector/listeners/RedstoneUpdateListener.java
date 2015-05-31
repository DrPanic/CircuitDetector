package io.github.netdex.CircuitDetector.listeners;

import io.github.netdex.CircuitDetector.CircuitDetector;
import io.github.netdex.CircuitDetector.util.Util;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockRedstoneEvent;

/**
 * A listener which listens for changes in redstone events, and then adds a violation
 */
public class RedstoneUpdateListener implements Listener {
	
	private CircuitDetector cd;
	private HashMap<Location, Integer> VIOLATIONS;
	
	public RedstoneUpdateListener(CircuitDetector cd, HashMap<Location, Integer> violations){
		this.cd = cd;
		this.VIOLATIONS = violations;
	}
	
	@EventHandler
	public void onBlockRedstoneChange(BlockRedstoneEvent event){
		Block b = event.getBlock();
		if(event.getOldCurrent() == 0){
			createViolation(b);
		}
	}
	
	@EventHandler
	public void onPistonExtendEvent(BlockPistonExtendEvent event){
		Block b = event.getBlock();
		createViolation(b);
	}

	public void createViolation(Block b){
		if(Util.isRedstone(b)){
			Location loc = b.getLocation();
			
			// If this violation is new, give it a count of 1
			if(VIOLATIONS.get(loc) == null)
				VIOLATIONS.put(b.getLocation(), 1);
			// Add 1 to the violation count
			else
				VIOLATIONS.put(b.getLocation(), VIOLATIONS.get(loc) + 1);
			
			// Send a message to all players who have logging enabled
			for(UUID uuid : cd.playersLogging.keySet()){
				if(cd.playersLogging.get(uuid)){ 
					Player player = Bukkit.getPlayer(uuid);
					
					String formattedLocation = Util.formatLocation(b.getLocation());
					String msg = ChatColor.BLUE + Util.getDate() + ChatColor.DARK_GRAY + " : " + ChatColor.AQUA + "\"" + ChatColor.ITALIC + b.getType().name() 
							+ ChatColor.AQUA + "\" at " + ChatColor.GRAY + formattedLocation 
							+ ChatColor.DARK_RED + " x" + VIOLATIONS.get(b.getLocation());
					player.sendMessage(msg);
				}
			}
			
			// If the threshold is passed, destroy the circuit
			if(VIOLATIONS.get(b.getLocation()) > cd.THRESHOLD && cd.THRESHOLD != 0){
				Util.destroyCircuit(b, true);
				
				for(UUID uuid : cd.playersLogging.keySet()){
					if(cd.playersLogging.get(uuid)){ 
						Player player = Bukkit.getPlayer(uuid);
						
						String formattedLocation = Util.formatLocation(b.getLocation());
						String msg = ChatColor.BLUE + Util.getDate() + ChatColor.DARK_GRAY + " : " + ChatColor.AQUA + 
								"Circuit has been destroyed at " + ChatColor.GRAY + formattedLocation + ChatColor.AQUA + ".";
						
						player.sendMessage(msg);
					}
				}
			}
		}
	}
}
