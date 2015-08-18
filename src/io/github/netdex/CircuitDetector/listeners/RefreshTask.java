package io.github.netdex.CircuitDetector.listeners;

import io.github.netdex.CircuitDetector.CircuitDetector;

public class RefreshTask implements Runnable {
	
	public RefreshTask(){
	}
	@Override
	public void run(){
		try {
			CircuitDetector.VIOLATIONS.clear();
		} catch (Exception e) {}
	}
}
