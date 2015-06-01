package io.github.netdex.CircuitDetector;

import io.github.netdex.CircuitDetector.util.Util;

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

	private CircuitDetector cd;
	public CommandManager(CircuitDetector cd) {
		this.cd = cd;
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
				Util.sendMessage(player, "Circuit Detector v" + cd.getDescription().getVersion());
				String[] help = new String[] { 
						ChatColor.AQUA + "/cd stats " + ChatColor.BLUE + "| Gets statistics and other data", 
						ChatColor.AQUA + "/cd log " + ChatColor.BLUE + "| Enables logging",
						ChatColor.AQUA + "/cd unlog " + ChatColor.BLUE + "| Disables logging", ChatColor.AQUA + "/cd list " + ChatColor.BLUE + "| Gets violators",
						ChatColor.AQUA + "/cd kill (<x> <y> <z>)  " + ChatColor.BLUE + "| Kills any redstone circuit at XYZ by destroying it",
						ChatColor.AQUA + "/cd set ((threshold/refresh) <int>)  " + ChatColor.BLUE + "| Sets some variables" };
				player.sendMessage(help);
				return true;
			}
			else if (arg.equalsIgnoreCase("stats")) {
				Util.sendMessage(player, "Statistics");
				String[] stats = new String[] { 
						ChatColor.AQUA + "Total Existing Violators: " + ChatColor.BLUE + cd.violations.size(),
						ChatColor.AQUA + "Number of Players Currently Logging: " + ChatColor.BLUE + cd.playersLogging.size(),
						ChatColor.RED + "That's it for now, more to be added in the future"
				};
				player.sendMessage(stats);
				return true;
			} else if (arg.equalsIgnoreCase("log")) {
				UUID playerUUID = player.getUniqueId();
				if (cd.playersLogging.get(playerUUID) == null || !cd.playersLogging.get(playerUUID)) {
					cd.playersLogging.put(player.getUniqueId(), true);
					Util.sendMessage(player, "Logging enabled.");
				} else {
					cd.playersLogging.put(player.getUniqueId(), false);
					Util.sendMessage(player, "Logging disabled.");
				}

				return true;
			}
			else if (arg.equalsIgnoreCase("unlog")) {
				cd.playersLogging.put(player.getUniqueId(), false);
				Util.sendMessage(player, "Logging disabled.");
				return true;
			}
			else if (arg.equalsIgnoreCase("list")) {
				Util.sendMessage(player, "Violators:");

				if (cd.violations.size() == 0) {
					player.sendMessage(ChatColor.BLUE + "No violations.");
					return true;
				} else {
					int c = 1;
					for (Location loc : cd.violations.keySet()) {
						Block b = loc.getBlock();
						player.sendMessage(ChatColor.BLUE + "" + c + ". " + ChatColor.AQUA + "\"" + b.getType().name() + "\" at " + ChatColor.GRAY + Util.formatLocation(loc) + ChatColor.DARK_RED
								+ " x" + cd.violations.get(loc));
						c++;
					}
				}
				return true;
			}
			else if (arg.equalsIgnoreCase("kill")) {
				Location loc;
				
				if (args.length < 4){
					Util.sendMessage(player, "No coordinates specified. Assuming current location.");
					loc = player.getLocation();
				}
				else{
					int x,y,z = 0;
					try {
						x = Integer.parseInt(args[1]);
						y = Integer.parseInt(args[2]);
						z = Integer.parseInt(args[3]);
					} catch (NumberFormatException nfe) {
						Util.sendMessage(player, "Invalid coordinates, must be integers.");
						return true;
					}
					loc = new Location(player.getWorld(), x, y, z);
				}
				
				Block b = loc.getBlock();
				if (!Util.isRedstone(b)) {
					Util.sendMessage(player, "There are no circuits at the specified coordinates.");
					return true;
				}
				
				Util.destroyCircuit(b, true);
				Util.sendMessage(player, String.format("Circuit starting at %s was destroyed.", Util.formatLocation(loc)));
				return true;
			}

			// Sets the threshold that a circuit must meet before being
			// destroyed automatically. Defaults to 0.
			else if (arg.equalsIgnoreCase("set")) {
				if (args.length < 3) {
					Util.sendMessage(player, "Variables [you must reload after changing!]");
					String[] vars = new String[]{
							ChatColor.AQUA + "threshold: " + ChatColor.RED + " x" + cd.THRESHOLD + " " + ChatColor.BLUE + ": The amount of violations a block is allowed to have before it is destroyed.",
							ChatColor.AQUA + "refresh: " + ChatColor.RED + " " + cd.REFRESH_TIME + "s " + ChatColor.BLUE + ": The amount of time in seconds to periodically clear all violations."
					};
					player.sendMessage(vars);
					return true;
				}
				else{
					String variable = args[1];
					int var = 0;
					try {
						var = Integer.parseInt(args[2]);
					} catch (NumberFormatException nfe) {
						Util.sendMessage(player, "Variable must be an integer.");
						return true;
					}
					if (var < 0) {
						Util.sendMessage(player, "Variable cannot be negative.");
						return true;
					}
					if (variable.equalsIgnoreCase("threshold")) {
						if(var == 0){
							Util.sendMessage(player, "Threshold set to 0. Will not auto-destroy clocks.");
							cd.THRESHOLD = 0;
						}
						else{
							Util.sendMessage(player, String.format("Threshold set to %d.", var));
							cd.THRESHOLD = var;
						}
						return true;
					} else if (variable.equalsIgnoreCase("refresh")) {
						if(var == 0){
						}
						else{
							Util.sendMessage(player, "Refresh time set to " + var + ".");
							cd.REFRESH_TIME = var;
						}
						return true;
					} else {
						Util.sendMessage(player, "Invalid variable. '/cd set' for usage.");
					}
				}
				return true;
			}
		}
		return false;
	}
}
