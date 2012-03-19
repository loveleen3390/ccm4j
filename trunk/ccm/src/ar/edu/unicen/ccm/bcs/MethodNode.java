package ar.edu.unicen.ccm.bcs;

import java.util.Map;
import java.util.Stack;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;

import ar.edu.unicen.ccm.model.DependencyModel;

public class MethodNode {
	//this is used as the key to lookup methods in the graph
	MethodSignature methodSignature;
	
	//DirectedGraph<MethodNode, DefaultEdge> graph;
	DependencyModel dependencyModel;
	private Map<MethodSignature, MethodNode> map;
	
	public MethodDeclaration md;
	
	int cost; //complete cost
	int flatCost; //"flat" cost: don't take recursion into account
	
	String expr;
	
	boolean recursive;
	
	public MethodNode(MethodDeclaration md, DependencyModel depModel, Map<MethodSignature, MethodNode> map) {
		this.methodSignature = MethodSignature.from(md.resolveBinding());
		this.md = md;
		this.dependencyModel = depModel;
		this.map = map;
		this.cost = -1; 
		this.flatCost = -1;
		this.recursive = false;
	}

	public MethodSignature getSignature() {
		return methodSignature;
	}

	
	public String getExpr() {
		return this.expr;
	}
	
	public int getCost() {
		Stack<MethodSignature> stack = new Stack<MethodSignature>();
		stack.add(this.methodSignature);
		return getCost(stack);
	}
	public int getCost(Stack<MethodSignature> callStack) {
		
		if (this.cost != -1) {
			return this.cost;
		} else {
			int calculatedCost = calculateCost(callStack);
			if (!this.recursive)  //recursive methods aren't memorized, explain why latter
				this.cost = calculatedCost;
			return calculatedCost;
		}
	}
	
	
	
	private int calculateCost(Stack<MethodSignature> callStack) {
		IMethodBinding mb = md.resolveBinding();
		if (mb.getDeclaringClass().isInterface() ||
				Modifier.isAbstract(md.getModifiers()))
				return 1; // NOTE: this will never be called, we handle this case in WCCVisitor
		else {
				WCCVisitor visitor = new WCCVisitor(this, this.dependencyModel, map, callStack);
				this.md.accept(visitor);
				
				this.expr = visitor.getExpr();
				return visitor.getCost();
		}
	}
	
	public String toString() {
		return this.methodSignature.toString();
	}
	
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof MethodNode ) {
			MethodNode other = (MethodNode) obj;
			return this.methodSignature.equals(other.methodSignature);
		} 
		return false;
	}
	
	@Override
	public int hashCode() {
		return this.methodSignature.hashCode();
	}
	
	public void setRecursive() {
		this.recursive = true;
	}
	
}
