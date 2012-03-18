package ar.edu.unicen.ccm.bcs;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

import ar.edu.unicen.ccm.WeightFactors;
import ar.edu.unicen.ccm.model.DependencyModel;

public class WCCVisitor extends ASTVisitor {
	public int cost;
	DependencyModel depModel;
	Map<MethodSignature, MethodNode> methodMap;
	MethodNode currentMethod;
	boolean isFlatCost;
	StringBuffer expr;
	
	public WCCVisitor(MethodNode currentMethod, DependencyModel depModel, Map<MethodSignature, MethodNode> methodMap, boolean isFlatCost) {
		this.cost = 0;
		this.currentMethod = currentMethod;
		this.isFlatCost = isFlatCost;
		this.depModel = depModel;
		this.methodMap = methodMap;
		this.expr = new StringBuffer();
	}

	public int getCost() {
		return cost;
	}
	
	public String getExpr() {
		return this.expr.toString();
	}
	
	@Override
	public boolean visit(MethodDeclaration node) {
		//Sequence: only 1 for each method, at the top level.
		cost += WeightFactors.sequenceWeight();
		expr.append(WeightFactors.sequenceWeight() +" ");
		return super.visit(node);
	}
	
	@Override
	public boolean visit(DoStatement node) {
		return visitNestedStruct(WeightFactors.loopFactor(), node.getExpression(), new Statement[]{node.getBody()});
	}
	
	@Override
	public boolean visit(EnhancedForStatement node) {
		return visitNestedStruct(WeightFactors.loopFactor(), node.getExpression(), new Statement[]{node.getBody()});
	}
	@Override
	public boolean visit(ForStatement node) {
		return visitNestedStruct(WeightFactors.loopFactor(), node.getExpression(), new Statement[]{node.getBody()});
	}
	
	@Override
	public boolean visit(IfStatement node) {
		return visitNestedStruct(WeightFactors.conditionalFactor(), node.getExpression(), new Statement[]{node.getThenStatement(), node.getElseStatement()});
	}
	@Override
	public boolean visit(SwitchStatement node) {
		List<Statement> c = (List<Statement>)node.statements();
		Statement[] childs = new Statement[c.size()];
		for(int i=0; i<childs.length;i++)
			childs[i] = c.get(i);

		return visitNestedStruct(WeightFactors.switchFactor(), node.getExpression(), childs);
	}
	
	@Override
	public boolean visit(WhileStatement node) {
		return visitNestedStruct(WeightFactors.loopFactor(), node.getExpression(), new Statement[]{node.getBody()});
	}

	@Override
	public boolean visit(ClassInstanceCreation node) {
		IMethodBinding mb = node.resolveConstructorBinding();
		return doVisitMethodInvocation(mb);
	}
	
	@Override
	public boolean visit(MethodInvocation node) {
		IMethodBinding mb = node.resolveMethodBinding();
		return doVisitMethodInvocation(mb);
	}
		
	
	
	
	
	private boolean visitNestedStruct(int factor, Expression exprStatement, Statement[] nested) {
		WCCVisitor childVisitor = newChildVisitor();
		if (exprStatement != null)
			exprStatement.accept(childVisitor); // for expressions might be null
		
		cost += childVisitor.getCost();
		expr.append(" + (" + childVisitor.getCost() + ")");
				
		
		childVisitor = newChildVisitor();
		for(Statement child : nested)
			if (child != null)
				child.accept(childVisitor);  //else branch may be null
		int childCost = childVisitor.getCost();
		if (childCost == 0)
			childCost = 1; //we multiply, so nested cost can't be zero.  
		
		expr.append(" + " + factor + " * [" + childVisitor.getExpr() + "]");

		cost += factor * childCost;
		return false;
	}
	
	public boolean doVisitMethodInvocation(IMethodBinding mb) {
		MethodSignature targetSignature = MethodSignature.from(mb);
		if (this.methodMap.containsKey(targetSignature)) {
			//now check for cycles
			MethodNode target = this.methodMap.get(targetSignature);
			if (mb.getDeclaringClass() ==
					this.currentMethod.md.resolveBinding().getDeclaringClass() ) {
				//TODO: and if it is one of its superclasses?
				cost += WeightFactors.methodCallWeight();
				expr.append(" + " + WeightFactors.methodCallWeight());
			}
			else {
				Set<MethodSignature> methodsInLoop = this.depModel.getRecursivePath(this.currentMethod.getSignature());
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

	
	private WCCVisitor newChildVisitor() {
		return new WCCVisitor(this.currentMethod, this.depModel, this.methodMap, this.isFlatCost);
	}
	
}


