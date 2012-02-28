package ar.edu.unicen.ccm.model;

import java.util.Map;

import ar.edu.unicen.ccm.bcs.MethodNode;

public class ClassComplexityInfo {
	String name;
	int attrComplexity;
	Map<String, MethodNode> methods;

	public ClassComplexityInfo(String name, int attrComplexity, Map<String, MethodNode> methods) {
		this.name = name;
		this.attrComplexity = attrComplexity;
		this.methods = methods;
	}
	
	public String getName() {
		return name;
	}
	
	public int getAttrComplexity() {
		return attrComplexity;
	}
	
	public Map<String, MethodNode> getMethods() {
		return methods;
	}
	
	
	public int getWeightedClassComplexity() {
		int r = attrComplexity;
		for (MethodNode v : methods.values())
				r += v.getCost();
		return r;
	}

}
