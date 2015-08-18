package io.github.netdex.CircuitDetector.listeners;

import static org.bukkit.ChatColor.AQUA;
import static org.bukkit.ChatColor.BLUE;
import static org.bukkit.ChatColor.DARK_GRAY;
import static org.bukkit.ChatColor.DARK_RED;
import static org.bukkit.ChatColor.GRAY;
import static org.bukkit.ChatColor.ITALIC;
import static org.bukkit.ChatColor.UNDERLINE;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockRedstoneEvent;

import io.github.netdex.CircuitDetector.CircuitDetector;
import io.github.netdex.CircuitDetector.util.Util;
import io.github.netdex.CircuitDetector.util.Violation;
import mkremins.fanciful.FancyMessage;
/**
 * A listener which listens for changes in redstone events, and then adds a violation
 */
public class RedstoneUpdateListener implements Listener {
	
	private CircuitDetector cd;
	
	public RedstoneUpdateListener(CircuitDetector cd){
		this.cd = cd;
	}
	
	@EventHandler
	public void onBlockRedstoneChange(BlockRedstoneEvent event){
		Block b = event.getBlock();
		if(event.getOldCurrent() == 0){
			registerViolation(b);
		}
	}
	
	@EventHandler
	public void onPistonExtendEvent(BlockPistonExtendEvent event){
		Block b = event.getBlock();
		registerViolation(b);
	}
	
	@EventHandler
	public void onPlayerLeave(org.bukkit.event.player.PlayerQuitEvent event){
		Player player = event.getPlayer();
		CircuitDetector.LOGGING.remove(player.getUniqueId());
	}

	public void registerViolation(Block b){
		if(Util.isRedstone(b)){
			Location loc = b.getLocation();
			
			// If this violation is new, give it a count of 1
			Violation v;
			if((v = CircuitDetector.getViolation(loc)) == null){
				v = new Violation(loc, System.currentTimeMillis());
				CircuitDetector.VIOLATIONS.add(v);
			}
			// Add 1 to the violation count
			else
				v.addInstance();
			
			// Send a message to all players who have logging enabled
			for(UUID uuid : CircuitDetector.LOGGING.keySet()){
				if(CircuitDetector.LOGGING.get(uuid)){ 
					Player player = Bukkit.getPlayer(uuid);
					
					v.getLogMessage().send(player);;
				}
			}
			
			// If the threshold is passed, destroy the circuit
			if(v.getInstances() > cd.THRESHOLD && cd.THRESHOLD != 0){
				Util.destroyCircuit(b, true);
				
				for(UUID uuid : CircuitDetector.LOGGING.keySet()){
					if(CircuitDetector.LOGGING.get(uuid)){ 
						Player player = Bukkit.getPlayer(uuid);
						
						String formattedLocation = Util.formatLocation(b.getLocation());
						String msg = BLUE + Util.getDate() + DARK_GRAY + " : " + AQUA + 
								"Circuit has been destroyed at " + GRAY + formattedLocation + AQUA + ".";
						
						player.sendMessage(msg);
					}
				}
			}
		}
	}
}
