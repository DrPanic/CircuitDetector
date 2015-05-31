package io.github.netdex.CircuitDetector.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

public class Util {

	/**
	 * A lazy method for sending a message with proper formatting.
	 * 
	 * @param player The player to send the message to
	 * @param s The message to send
	 */
	public static void sendMessage(Player player, String s) {
		player.sendMessage(ChatColor.BLUE + "" + ChatColor.BOLD + "[CircuitDetector] " + ChatColor.AQUA + s);
	}

	/**
	 * A method for getting the current time for timestamping redstone events.
	 * 
	 * @return the current time
	 */
	public static String getDate() {
		Calendar currentDate = Calendar.getInstance();
		SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
		String dateNow = formatter.format(currentDate.getTime());
		return dateNow;
	}

	/**
	 * Checks whether a block is redstone related or not. <br>
	 * Redstone related blocks include REDSTONE_WIRE, DIODE_BLOCK_OFF,
	 * DIODE_BLOCK_ON, REDSTONE_TORCH_OFF, REDSTONE_TORCH_ON
	 * 
	 * @param b The block to check
	 * @return if the block is a redstone related block or not
	 */
	
	public static boolean isRedstone(Block b) {
		if (b.getType() == Material.REDSTONE_WIRE || b.getType() == Material.DIODE_BLOCK_OFF || b.getType() == Material.DIODE_BLOCK_ON || b.getType() == Material.REDSTONE_TORCH_OFF
				|| b.getType() == Material.REDSTONE_TORCH_ON || b.getType() == Material.PISTON_BASE || b.getType() == Material.PISTON_EXTENSION || b.getType() == Material.PISTON_MOVING_PIECE
				|| b.getType() == Material.PISTON_STICKY_BASE) {
			return true;
		}
		return false;
	}

	/**
	 * Formats a location into a readable string.
	 * 
	 * @param loc The location to format
	 * @return the location in a readable string
	 */
	
	public static String formatLocation(Location loc) {
		return "[" + loc.getWorld().getName() + "; " + loc.getX() + ", " + loc.getY() + ", " + loc.getZ() + "]";
	}

	/**
	 * Recursively destroys a circuit. Starting at the block, it looks for
	 * neighboring blocks which redstone current can propagate to.<br>
	 * If a block is beside it and would be powered, then the method is called
	 * but with allowBlockBreak = false, which prevents further blocks from being broken, 
	 * destroying the entire world. (Boy is this hard to explain :P)
	 * 
	 * @param b The place to start recursively destroying the circuit
	 * @param allowBlockBreak Whether or not the last block broken was solid or
	 *            not. Should always be true, only the recursive method itself
	 *            should ever input false.
	 */
	
	public static void destroyCircuit(Block b, boolean allowBlockBreak) {
		b.getLocation().getWorld().createExplosion(b.getLocation(), 0);
		if (Util.isRedstone(b)) {
			b.setType(Material.AIR);
		}
		for (BlockFace face : BlockFace.values()) {
			if (face == BlockFace.UP || face == BlockFace.DOWN || face == BlockFace.NORTH || face == BlockFace.SOUTH || face == BlockFace.EAST || face == BlockFace.WEST) {
				Block side = b.getRelative(face);
				if (side.getType().isSolid() && allowBlockBreak) {
					destroyCircuit(side, false);
				} else if (Util.isRedstone(side)) {
					destroyCircuit(side, true);
				}
			}
		}

	}
}
