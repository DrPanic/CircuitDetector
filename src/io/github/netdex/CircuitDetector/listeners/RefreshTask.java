package io.github.netdex.CircuitDetector.listeners;

import java.util.HashMap;

import org.bukkit.Location;

public class RefreshTask implements Runnable {
	
	private HashMap<Location, Integer> violations;
	public RefreshTask(HashMap<Location, Integer> violations){
		this.violations = violations;
	}
	@Override
	public void run(){
		try {
			violations.clear();
		} catch (Exception e) {}
	}
}
