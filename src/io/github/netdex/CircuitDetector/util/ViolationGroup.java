package io.github.netdex.CircuitDetector.util;

import java.util.ArrayList;

public class ViolationGroup {

	private ArrayList<Violation> similarViolations;

	public ViolationGroup() {
		similarViolations = new ArrayList<Violation>();
	}

	public void addViolation(Violation v) {
		similarViolations.add(v);
	}

	public Violation getBaseViolation() {
		if (similarViolations.size() > 0)
			return similarViolations.get(0);
		return null;
	}
}
