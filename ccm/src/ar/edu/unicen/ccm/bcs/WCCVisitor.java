package ar.edu.unicen.ccm.bcs;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

import ar.edu.unicen.ccm.WeightFactors;
import ar.edu.unicen.ccm.model.DependencyModel;

public class WCCVisitor extends ASTVisitor {
	public int cost;
	DependencyModel depModel;
	Map<String, MethodNode> methodMap;
	MethodNode currentMethod;
	boolean isFlatCost;
	StringBuffer expr;
	
	public WCCVisitor(MethodNode currentMethod, DependencyModel depModel, Map<String, MethodNode> methodMap, boolean isFlatCost, int initialValue) {
		this.cost = initialValue;
		this.currentMethod = currentMethod;
		this.isFlatCost = isFlatCost;
		this.depModel = depModel;
		this.methodMap = methodMap;
		this.expr = new StringBuffer();
		if (initialValue != 0) {
			expr.append("+" + initialValue);
		}
	}

	public int getCost() {
		return cost;
	}
	
	public String getExpr() {
		return this.expr.toString();
	}
	
	@Override
	public boolean visit(DoStatement node) {
		
		WCCVisitor childVisitor = newChildVisitor(0);
		node.getExpression().accept(childVisitor);
		cost += childVisitor.getCost();
		
		expr.append(" + (" + childVisitor.getCost() + ")");
		
		
		childVisitor = newChildVisitor(WeightFactors.sequenceWeight());
		node.getBody().accept(childVisitor);
		expr.append(" + " + WeightFactors.loopFactor() + " * [" + childVisitor.getExpr() + "]");
		cost += childVisitor.getCost() * WeightFactors.loopFactor();
		return false;
	}
	
	@Override
	public boolean visit(EnhancedForStatement node) {
		WCCVisitor childVisitor = newChildVisitor(0);
		node.getExpression().accept(childVisitor);
		cost += childVisitor.getCost();
		expr.append(" + (" + childVisitor.getCost() + ")");
				
		
		childVisitor = newChildVisitor(WeightFactors.sequenceWeight());
		node.getBody().accept(childVisitor);
		expr.append(" + " + WeightFactors.loopFactor() + " * [" + childVisitor.getExpr() + "]");
		
		cost += childVisitor.getCost() * WeightFactors.loopFactor();
		return false;
	}
	@Override
	public boolean visit(ForStatement node) {
		WCCVisitor childVisitor = newChildVisitor(0);
		node.getExpression().accept(childVisitor);
		cost += childVisitor.getCost();
		expr.append(" + (" + childVisitor.getCost() + ")");
					
		
		childVisitor = newChildVisitor(WeightFactors.sequenceWeight());
		node.getBody().accept(childVisitor);
		expr.append(" + " + WeightFactors.loopFactor() + " * [" + childVisitor.getExpr() + "]");
		
		cost += childVisitor.getCost() * WeightFactors.loopFactor();
		return false;
	}
	
	@Override
	public boolean visit(IfStatement node) {
		WCCVisitor childVisitor = newChildVisitor(0);
		node.getExpression().accept(childVisitor);
		cost += childVisitor.getCost();
		expr.append(" + (" + childVisitor.getCost() + ")");
		
		
		childVisitor = newChildVisitor(WeightFactors.sequenceWeight());
		node.getThenStatement().accept(childVisitor);
		if (node.getElseStatement() != null)
			node.getElseStatement().accept(childVisitor);
		
		expr.append(" + " + WeightFactors.conditionalFactor() + " * [" + childVisitor.getExpr() + "]");
		cost += WeightFactors.conditionalFactor() * childVisitor.getCost();
		return false;
	}
	@Override
	public boolean visit(MethodInvocation node) {
		IMethodBinding mb = node.resolveMethodBinding();
		String targetSignature = MethodSignature.from(mb);
		if (this.methodMap.containsKey(targetSignature)) {
			//now check for cycles
			MethodNode target = this.methodMap.get(targetSignature);
			if (mb.getDeclaringClass() ==
					this.currentMethod.md.resolveBinding().getDeclaringClass() ) {
				cost += WeightFactors.methodCallWeight();
				expr.append(" + " + WeightFactors.methodCallWeight());
			}
			else {
				Set<String> methodsInLoop = this.depModel.getRecursivePath(this.currentMethod.getSignature());
				if (this.isFlatCost)  {
					if (methodsInLoop.contains(targetSignature)) {
						expr.append(" + " + WeightFactors.methodCallWeight());
						cost += WeightFactors.methodCallWeight();  //do not enter the loop
					}
					else {	
						
						int callCost = target.getCost();
						cost += WeightFactors.methodCallWeight() + callCost;
						expr.append(" + " + WeightFactors.methodCallWeight() + " + [" + callCost + "]");
					}
				} else {
					if (methodsInLoop.contains(targetSignature)) {
						int callCost = target.getFlatCost();
						cost += WeightFactors.recursiveCalllWeight() + callCost;
						expr.append(" + " + WeightFactors.recursiveCalllWeight() + " + [" + callCost + "]");
					}
					else { 
						int callCost = target.getCost();
						cost += WeightFactors.methodCallWeight() + callCost;
						expr.append(" + " + WeightFactors.methodCallWeight() +" + [" + callCost + "]");
					}
				}
			
			}
		}
		//otherwise is a method defined elsewere, not in this project.
		return true; //for parameters
	}
	
	@Override
	public boolean visit(SwitchStatement node) {
		WCCVisitor childVisitor = newChildVisitor(0);
		node.getExpression().accept(childVisitor);
		cost += childVisitor.getCost();
		expr.append(" + (" + childVisitor.getCost() + ")");

		childVisitor = newChildVisitor(WeightFactors.sequenceWeight());
		for (Statement s : (List<Statement>)node.statements())
			s.accept(childVisitor);
		cost += WeightFactors.switchFactor() * childVisitor.getCost();
		expr.append(" + " + WeightFactors.switchFactor() + " * [" + childVisitor.getExpr() + "]");
		return false;
	}
	
	@Override
	public boolean visit(WhileStatement node) {
		WCCVisitor childVisitor = newChildVisitor(0);
		node.getExpression().accept(childVisitor);
		cost += childVisitor.getCost();
		expr.append(" + (" + childVisitor.getCost() + ")");
		
		
		
		childVisitor = newChildVisitor(WeightFactors.sequenceWeight());
		node.getBody().accept(childVisitor);
		expr.append(" + " + WeightFactors.loopFactor() + " * [" + childVisitor.getExpr() + "]");

		cost += WeightFactors.loopFactor()* childVisitor.getCost();
		
		return false;
	}
	
	private WCCVisitor newChildVisitor(int initialValue) {
		return new WCCVisitor(this.currentMethod, this.depModel, this.methodMap, this.isFlatCost, initialValue);
	}
	
}


