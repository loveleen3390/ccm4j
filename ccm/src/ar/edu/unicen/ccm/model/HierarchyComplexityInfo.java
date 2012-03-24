package ar.edu.unicen.ccm.model;

import java.math.BigInteger;

public class HierarchyComplexityInfo {

	int depth = 0;
	int classes = 0;
	BigInteger cost =BigInteger.valueOf(0);
	String expr;
	
	public HierarchyComplexityInfo(BigInteger cost, int depth, int classes, String expr) {
		this.cost = cost;
		this.depth = depth;
		this.classes = classes;
		this.expr = expr;
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
	
	public String getExpr() {
		return expr;
	}
	
	
}
