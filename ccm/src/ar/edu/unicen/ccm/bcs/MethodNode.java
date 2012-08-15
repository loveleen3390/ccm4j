package ar.edu.unicen.ccm.bcs;

import java.math.BigInteger;
import java.util.Map;
import java.util.Stack;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;

import ar.edu.unicen.ccm.model.CostModel;

public class MethodNode {
	//this is used as the key to lookup methods in the graph
	MethodSignature methodSignature;
	
	//DirectedGraph<MethodNode, DefaultEdge> graph;
	CostModel costModel;
	private Map<MethodSignature, MethodNode> map;
	
	public MethodDeclaration md;
	
	BigInteger cost; //complete cost
	BigInteger flatCost; //"flat" cost: don't take recursion into account
	
	String expr;
	
	boolean recursive;
	
	/**
	 * @author mcrasso To account external calls per method 
	 */
	private int externalCalls;
	
	public void incExternalCalls() {
		this.externalCalls++;
	}
	
	public int getExternalCalls() {
		return externalCalls;
	}
	
	public MethodNode(MethodDeclaration md, CostModel costModel, Map<MethodSignature, MethodNode> map) {
		this.methodSignature = MethodSignature.from(md.resolveBinding());
		this.md = md;
		this.costModel = costModel;
		this.map = map;
		this.cost = null; 
		this.flatCost = null;
		this.recursive = false;
	}

	public MethodSignature getSignature() {
		return methodSignature;
	}

	
	public String getExpr() {
		return this.expr;
	}
	
	public BigInteger getCost() {
		Stack<MethodSignature> stack = new Stack<MethodSignature>();
		stack.add(this.methodSignature);
		return getCost(stack);
	}
	public BigInteger getCost(Stack<MethodSignature> callStack) {
		
		if (this.cost != null) {
			return this.cost;
		} else {
			BigInteger calculatedCost = calculateCost(callStack);
			if (!this.recursive) { //recursive methods aren't memorized, explain why latter
				this.cost = calculatedCost;
				this.md = null; //we won't use it anymore
			}
			return calculatedCost;
		}
	}
	
	public CostModel getCostModel() {
		return costModel;
	}
	
	private BigInteger calculateCost(Stack<MethodSignature> callStack) {
		WCCVisitor visitor = new WCCVisitor(this, this.costModel, map, callStack);
		this.md.accept(visitor);
		this.expr = visitor.getExpr();
		return visitor.getCost();
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
