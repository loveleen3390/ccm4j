package ar.edu.unicen.ccm.bcs;

import java.util.Map;

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
	
	public MethodNode(MethodDeclaration md, DependencyModel depModel, Map<MethodSignature, MethodNode> map) {
		this.methodSignature = MethodSignature.from(md.resolveBinding());
		this.md = md;
		this.dependencyModel = depModel;
		this.map = map;
		this.cost = -1; 
		this.flatCost = -1;
	}

	public MethodSignature getSignature() {
		return methodSignature;
	}

	
	public String getExpr() {
		return this.expr;
	}
	public int getCost() {
		if (this.cost == -1)
			this.cost = calculateCost(false);

		return this.cost;
	}
	
	
	
	public int getFlatCost() {
		if (this.flatCost == -1)
			this.flatCost = calculateCost(true);
		
		return this.flatCost;
	}
	
	private int calculateCost(boolean flat) {
		IMethodBinding mb = md.resolveBinding();
		if (mb.getDeclaringClass().isInterface() ||
				Modifier.isAbstract(md.getModifiers()))
				return 1; // TODO:  average over all implementations..
		else {
				WCCVisitor visitor = new WCCVisitor(this, this.dependencyModel, map, true);
				this.md.accept(visitor);
				if (!flat) //Hack: this test is ugly
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
	
	
}
