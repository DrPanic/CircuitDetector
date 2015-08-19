package io.github.netdex.CircuitDetector;

import static org.bukkit.ChatColor.AQUA;
import static org.bukkit.ChatColor.BLUE;
import static org.bukkit.ChatColor.RED;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import io.github.netdex.CircuitDetector.util.Util;
import io.github.netdex.CircuitDetector.util.Violation;
import io.github.netdex.CircuitDetector.util.ViolationGroup;

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
						ChatColor.AQUA + "/cd set ((threshold/refresh) <int>)  " + ChatColor.BLUE + "| Sets some variables",
						ChatColor.AQUA + "/cd isolate " + ChatColor.BLUE + "| Attempts to group logging events into separate, isolated clocks."};
				player.sendMessage(help);
				return true;
			}
			else if (arg.equalsIgnoreCase("stats")) {
				Util.sendMessage(player, "Statistics");
				String[] stats = new String[] { 
						ChatColor.AQUA + "Total Existing Violators: " + ChatColor.BLUE + CircuitDetector.VIOLATIONS.size(),
						ChatColor.AQUA + "Number of Players Currently Logging: " + ChatColor.BLUE + CircuitDetector.LOGGING.size(),
						ChatColor.RED + "That's it for now, more to be added in the future"
				};
				player.sendMessage(stats);
				return true;
			} else if (arg.equalsIgnoreCase("log")) {
				UUID playerUUID = player.getUniqueId();
				if (CircuitDetector.LOGGING.get(playerUUID) == null || !CircuitDetector.LOGGING.get(playerUUID)) {
					CircuitDetector.LOGGING.put(player.getUniqueId(), true);
					Util.sendMessage(player, "Logging enabled.");
				} else {
					CircuitDetector.LOGGING.put(player.getUniqueId(), false);
					Util.sendMessage(player, "Logging disabled.");
				}

				return true;
			}
			else if (arg.equalsIgnoreCase("unlog")) {
				CircuitDetector.LOGGING.put(player.getUniqueId(), false);
				Util.sendMessage(player, "Logging disabled.");
				return true;
			}
			else if (arg.equalsIgnoreCase("list")) {
				Util.sendMessage(player, "Violators:");

				if (CircuitDetector.VIOLATIONS.size() == 0) {
					player.sendMessage(ChatColor.BLUE + "No violations.");
					return true;
				} else {
					// Sort the list of violations by the instances of the violation
					Collections.sort(CircuitDetector.VIOLATIONS, new Comparator<Violation>(){

						@Override
						public int compare(Violation a, Violation b) {
							if(a.getInstances() > b.getInstances())
								return -1;
							if(a.getInstances() < b.getInstances())
								return 1;
							return 0;
						}
						
					});
					int c = 0;
					for (Violation v : CircuitDetector.VIOLATIONS) {
						if(c >= 10)
							break;
						v.getLogMessage().send(player);
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
							AQUA + "threshold: " + RED + " x" + CircuitDetector.THRESHOLD + " " + BLUE + ": The amount of violations a block is allowed to have before it is destroyed.",
							AQUA + "refresh: " + RED + " " + CircuitDetector.REFRESH_TIME + "s " + BLUE + ": The amount of time in seconds to periodically clear all violations."
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
							CircuitDetector.THRESHOLD = 0;
						}
						else{
							Util.sendMessage(player, String.format("Threshold set to %d.", var));
							CircuitDetector.THRESHOLD = var;
						}
						return true;
					} else if (variable.equalsIgnoreCase("refresh")) {
						if(var == 0){
						}
						else{
							Util.sendMessage(player, "Refresh time set to " + var + ".");
							CircuitDetector.REFRESH_TIME = var;
						}
						return true;
					} else {
						Util.sendMessage(player, "Invalid variable. '/cd set' for usage.");
					}
				}
				return true;
			}
			else if(arg.equalsIgnoreCase("isolate")){
				if(CircuitDetector.VIOLATIONS.size() > 0){
					Util.sendMessage(player, "Showing isolated circuits: ");
					Collections.sort(CircuitDetector.VIOLATIONS, new Comparator<Violation>(){

						@Override
						public int compare(Violation a, Violation b) {
							if(a.getTimeDiff() > b.getTimeDiff())
								return -1;
							if(a.getTimeDiff() < b.getTimeDiff())
								return 1;
							return 0;
						}
						
					});
					ArrayList<ViolationGroup> isolated = new ArrayList<ViolationGroup>();
					ViolationGroup base = new ViolationGroup();
					base.addViolation(CircuitDetector.VIOLATIONS.get(0));
					isolated.add(base);
					for(int i = 0; i < CircuitDetector.VIOLATIONS.size() - 1; i++){
						Violation v1 = CircuitDetector.VIOLATIONS.get(i);
						Violation v2 = CircuitDetector.VIOLATIONS.get(i + 1);
						long diff = Math.abs(v1.getTimeDiff() - v2.getTimeDiff());
						if(diff > CircuitDetector.SIMILAR_CIRCUIT_DELAY_EPSILON){
							isolated.add(new ViolationGroup());
						}
						isolated.get(isolated.size() - 1).addViolation(v2);
					}
					for(int i = 0; i < isolated.size(); i++){
						ViolationGroup vg = isolated.get(i);
						vg.getBaseViolation().getLogMessage().send(player);
					}
				}
				else{
					Util.sendMessage(player, "No violations to isolate.");
				}
				return true;
			}
		}
		return false;
	}
}
