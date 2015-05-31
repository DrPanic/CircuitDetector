package io.github.netdex.CircuitDetector;

import io.github.netdex.CircuitDetector.util.Utility;

import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Handles all the commands of the plugin
 */
public class CommandManager implements CommandExecutor {

	public CommandManager() {

	}

	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		String cmd = command.getName();
		if (cmd.equalsIgnoreCase("cd")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage("You must be a player to use this command.");
				return true;
			}

			Player player = (Player) sender;

			if (args.length < 1)
				return false;

			String arg = args[0];

			if (arg.equalsIgnoreCase("help")) {
				Utility.sendMessage(player, "Help");
				String[] help = new String[] { ChatColor.AQUA + "/cd stats " + ChatColor.BLUE + "| Gets statistics and other data", ChatColor.AQUA + "/cd log " + ChatColor.BLUE + "| Enables logging",
						ChatColor.AQUA + "/cd unlog " + ChatColor.BLUE + "| Disables logging", ChatColor.AQUA + "/cd list " + ChatColor.BLUE + "| Gets violators",
						ChatColor.AQUA + "/cd kill <x> <y> <z>  " + ChatColor.BLUE + "| Kills any redstone circuit at XYZ by destroying it",
						ChatColor.AQUA + "/cd set (threshold/refresh) <int>  " + ChatColor.BLUE + "| Sets some variables" };
				player.sendMessage(help);
				return true;
			}
			// This command toggles logging
			else if (arg.equalsIgnoreCase("stats")) {
				Utility.sendMessage(player, "Statistics");
				String[] stats = new String[] { ChatColor.AQUA + "Threshold " + ChatColor.BLUE + ": " + CircuitDetector.THRESHOLD,
						ChatColor.AQUA + "Refresh Time " + ChatColor.BLUE + ": " + CircuitDetector.REFRESH_TIME };
				player.sendMessage(stats);
				return true;
			} else if (arg.equalsIgnoreCase("log")) {
				UUID playerUUID = player.getUniqueId();
				if (CircuitDetector.LOGGING.get(playerUUID) == null || !CircuitDetector.LOGGING.get(playerUUID)) {
					CircuitDetector.LOGGING.put(player.getUniqueId(), true);
					Utility.sendMessage(player, "Logging enabled.");
				} else {
					CircuitDetector.LOGGING.put(player.getUniqueId(), false);
					Utility.sendMessage(player, "Logging disabled.");
				}

				return true;
			}
			// This command disables logging
			else if (arg.equalsIgnoreCase("unlog")) {
				CircuitDetector.LOGGING.put(player.getUniqueId(), false);
				Utility.sendMessage(player, "Logging disabled.");
				return true;
			}
			// This command lists all violations
			else if (arg.equalsIgnoreCase("list")) {
				Utility.sendMessage(player, "Violators:");

				if (CircuitDetector.VIOLATIONS.size() == 0) {
					player.sendMessage(ChatColor.BLUE + "No violations.");
					return true;
				} else {
					int c = 1;
					for (Location loc : CircuitDetector.VIOLATIONS.keySet()) {
						Block b = loc.getBlock();
						player.sendMessage(ChatColor.BLUE + "" + c + ". " + ChatColor.AQUA + "\"" + b.getType().name() + "\" at " + ChatColor.GRAY + Utility.formatLocation(loc) + ChatColor.DARK_RED
								+ " x" + CircuitDetector.VIOLATIONS.get(loc));
						c++;
					}
				}
				return true;
			}

			// This command kills known redstone circuits
			// Requires 3 integer coordinates
			else if (arg.equalsIgnoreCase("kill")) {
				if (args.length < 4)
					return false;
				int x = 0;
				int y = 0;
				int z = 0;

				try {
					x = Integer.parseInt(args[1]);
					y = Integer.parseInt(args[2]);
					z = Integer.parseInt(args[3]);
				} catch (NumberFormatException nfe) {
					Utility.sendMessage(player, "Invalid coordinates.");
					return true;
				}

				Location loc = new Location(player.getWorld(), x, y, z);
				Block b = loc.getBlock();
				// Check if the target block is redstone before nuking it
				if (!Utility.isRedstone(b)) {
					Utility.sendMessage(player, "There are no circuits at the specified coordinates.");
					return true;
				}
				// Destroy the circuit using the recursive method
				Utility.destroyCircuit(b, true);
				// Let the user know
				Utility.sendMessage(player, "Circuit starting at " + Utility.formatLocation(loc) + " was destroyed.");
				return true;
			}

			// Sets the threshold that a circuit must meet before being
			// destroyed automatically. Defaults to 0.
			else if (arg.equalsIgnoreCase("set")) {
				boolean showVars = false;
				if (args.length < 3) {
					showVars = true;
				}

				if (!showVars) {
					String variable = args[1];
					int var = 0;
					try {
						var = Integer.parseInt(args[2]);
					} catch (NumberFormatException nfe) {
						Utility.sendMessage(player, "Variable must be an integer.");
						return true;
					}
					if (var < 0) {
						Utility.sendMessage(player, "Variable cannot be negative.");
						return true;
					}
					if (variable.equalsIgnoreCase("threshold")) {
						if(var == 0){
							Utility.sendMessage(player, "Threshold set to 0. Will not auto-destroy clocks.");
							CircuitDetector.THRESHOLD = 0;
						}
						else{
							Utility.sendMessage(player, "Threshold set to " + var + ".");
							CircuitDetector.THRESHOLD = var;
						}
						return true;
					} else if (variable.equalsIgnoreCase("refresh")) {
						if(var == 0){
						}
						else{
							Utility.sendMessage(player, "Refresh time set to " + var + ".");
							CircuitDetector.REFRESH_TIME = var;
						}
						return true;
					} else {
						showVars = true;
					}
				}
				if(showVars){
					Utility.sendMessage(player, "Variables [you must reload after changing!]");
					String[] vars = new String[]{
							ChatColor.AQUA + "threshold: " + ChatColor.RED + " x" + CircuitDetector.THRESHOLD + " " + ChatColor.BLUE + ": The amount of violations a block is allowed to have before it is destroyed.",
							ChatColor.AQUA + "refresh: " + ChatColor.RED + " " + CircuitDetector.REFRESH_TIME + "s " + ChatColor.BLUE + ": The amount of time in seconds to periodically clear all violations."
					};
					player.sendMessage(vars);
					return true;
				}
				return true;
			}
		}
		return false;
	}
}
