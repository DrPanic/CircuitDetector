package io.github.netdex.CircuitDetector.listeners;

import io.github.netdex.CircuitDetector.CircuitDetector;
import io.github.netdex.CircuitDetector.util.Util;

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
public class ExistenceTask implements Runnable {
	private CircuitDetector cd;

	public ExistenceTask(CircuitDetector cd) {
		this.cd = cd;
	}

	@Override
	public void run() {
		try {
			for (Location loc : cd.violations.keySet()) {
				Block b = loc.getBlock();
				if (!Util.isRedstone(b)) {
					cd.violations.remove(loc);
				}
			}
		} catch (Throwable e) {

		}
	}
}
