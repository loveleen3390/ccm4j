package ar.edu.unicen.ccm.model;

import java.util.Map;

import ar.edu.unicen.ccm.bcs.MethodNode;
import ar.edu.unicen.ccm.bcs.MethodSignature;

public class ClassComplexityInfo {
	String name;
	int attrComplexity;
	Map<MethodSignature, MethodNode> methods;

	public ClassComplexityInfo(String name, int attrComplexity, Map<MethodSignature, MethodNode> methods) {
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
	
	public Map<MethodSignature, MethodNode> getMethods() {
		return methods;
	}
	
	
	public int getWeightedClassComplexity() {
		int r = attrComplexity;
		for (MethodNode v : methods.values())
				r += v.getCost();
		return r;
	}

}
