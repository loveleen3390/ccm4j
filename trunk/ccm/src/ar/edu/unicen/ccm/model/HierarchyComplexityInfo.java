package ar.edu.unicen.ccm.model;

import java.math.BigDecimal;
import java.math.BigInteger;

public class HierarchyComplexityInfo {

	int depth = 0;
	int classes = 0;
	BigInteger cost =BigInteger.valueOf(0);
	
	public HierarchyComplexityInfo(BigInteger cost, int depth, int classes) {
		this.cost = cost;
		this.depth = depth;
		this.classes = classes;
	}
	
	void addClassCost(BigInteger c) {
		this.cost = this.cost.add(c);
		this.classes++;
	}
	
	void addDepth() {
		this.depth++;
	}
	
	public int getClasses() {
		return classes;
	}
	public BigInteger getCost() {
		return cost;
	}
	public int getDepth() {
		return depth;
	}
	
	
}
