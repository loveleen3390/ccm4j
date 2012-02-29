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
	/*
	public void addCallTo(IMethodBinding mb) {
		MethodNode called = new MethodNode(mb);
		System.out.println("Evaluating call to " + called);
		if (graph.containsVertex(called)) {
			//Otherwise it is a library api
			System.out.println(this + "->" + called);
			this.graph.addEdge(this, called);
		}
	}

	
	public void calculateDependencies() {
		DependencyVisitor v = new DependencyVisitor(this);
		this.md.getBody().accept(v);
	}
		*/
	
	public String getExpr() {
		return this.expr;
	}
	public int getCost() {
		if (this.cost == -1) {
			IMethodBinding mb = md.resolveBinding();
			if (mb.getDeclaringClass().isInterface() ||
					Modifier.isAbstract(md.getModifiers()))
				this.cost = 1; // TODO:  average over all implementations..
			else {
					WCCVisitor visitor = new WCCVisitor(this, this.dependencyModel, map, false);
					this.md.accept(visitor);
					this.cost = visitor.cost;
					this.expr = visitor.getExpr();
			}
		}
		return this.cost;
	}
	
	
	
	public int getFlatCost() {
		if (this.flatCost == -1) {
			IMethodBinding mb = md.resolveBinding();
			if (mb.getDeclaringClass().isInterface() ||
					Modifier.isAbstract(md.getModifiers()))
				this.flatCost = 1; // TODO:  average over all implementations..
			else {
					WCCVisitor visitor = new WCCVisitor(this, this.dependencyModel, map, true);
					this.md.accept(visitor);
					this.flatCost = visitor.cost;
			}
		}
		return this.flatCost;
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
	
	/*
	private class DependencyVisitor extends ASTVisitor {
		MethodNode node;
		public DependencyVisitor(MethodNode node) {
			this.node = node;
		}
		@Override
		public boolean visit(MethodInvocation mi) {
			node.addCallTo(mi.resolveMethodBinding());
			return super.visit(mi);
		}
	}
	*/
	
	
}
