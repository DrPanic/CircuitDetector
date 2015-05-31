package io.github.netdex.CircuitDetector.listeners;

import io.github.netdex.CircuitDetector.CircuitDetector;
import io.github.netdex.CircuitDetector.util.Utility;

import org.bukkit.Location;
import org.bukkit.block.Block;

/**
 * Used to make sure violators stored in the violators ArrayList still exist.<br>
 * The run() method continuously loops through all violators to make sure that
 * they are still a redstone type.<br>
 * Mainly used for cleaning up after explosions or a block under redstone is
 * broken, and the BlockBrokenEvent doesn't fire.
 * 
 * @param CircuitDetector
 *            plugin
 */
public class ExistenceListener implements Runnable {
	private CircuitDetector plugin;

	public ExistenceListener(CircuitDetector plugin) {
		this.plugin = plugin;
	}

	@Override
	public void run() {
		try {
			for (Location loc : plugin.getViolations().keySet()) {
				Block b = loc.getBlock();
				if (!Utility.isRedstone(b)) {
					plugin.getViolations().remove(loc); // Remove the violation
														// if the block no
														// longer exists
				}
			}
		} catch (Throwable e) {

		}
	}
}
