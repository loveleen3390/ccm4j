package ar.edu.unicen.ccm.bcs;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

import ar.edu.unicen.ccm.WeightFactors;
import ar.edu.unicen.ccm.bcs.abstractmethod.MethodWeightStrategy;
import ar.edu.unicen.ccm.model.CostModel;
import ar.edu.unicen.ccm.model.DependencyModel;

public class WCCVisitor extends ASTVisitor {
	public BigInteger cost;
	CostModel costModel;
	Map<MethodSignature, MethodNode> methodMap;
	MethodNode currentMethod;
	StringBuilder expr;
	Stack<MethodSignature> stack;
	
	public WCCVisitor(MethodNode currentMethod, CostModel costModel, Map<MethodSignature, MethodNode> methodMap,  Stack<MethodSignature> stack) {
		this.cost = BigInteger.valueOf(0);
		this.currentMethod = currentMethod;
		this.costModel = costModel;
		this.methodMap = methodMap;
		this.expr = new StringBuilder();
		this.stack = stack;
	}

	public BigInteger getCost() {
		return cost;
	}
	
	public String getExpr() {
		return this.expr.toString();
	}
	
	@Override
	public boolean visit(MethodDeclaration node) {
		//Sequence: only 1 for each method, at the top level.
		cost = cost.add(WeightFactors.sequenceWeight());
		expr.append(WeightFactors.sequenceWeight() +" ");
		return super.visit(node);
	}

	
	@Override
	public boolean visit(DoStatement node) {
		return visitNestedStruct(WeightFactors.loopFactor(), new Expression[]{node.getExpression()}, new Statement[]{node.getBody()});
	}
	
	@Override
	public boolean visit(EnhancedForStatement node) {
		return visitNestedStruct(WeightFactors.loopFactor(), new Expression[]{node.getExpression()}, new Statement[]{node.getBody()});
	}
	@Override
	public boolean visit(ForStatement node) {
		return visitNestedStruct(WeightFactors.loopFactor(), new Expression[]{node.getExpression()}, new Statement[]{node.getBody()});
	}
	
	@Override
	public boolean visit(IfStatement node) {
		return visitNestedStruct(WeightFactors.conditionalFactor(), new Expression[]{node.getExpression()}, new Statement[]{node.getThenStatement(), node.getElseStatement()});
	}
	@Override
	public boolean visit(SwitchStatement node) {
		List<Statement> c = (List<Statement>)node.statements();
		Statement[] childs = new Statement[c.size()];
		for(int i=0; i<childs.length;i++)
			childs[i] = c.get(i);

		return visitNestedStruct(WeightFactors.switchFactor(), new Expression[]{node.getExpression()}, childs);
	}
	
	@Override
	public boolean visit(WhileStatement node) {
		return visitNestedStruct(WeightFactors.loopFactor(), new Expression[]{node.getExpression()}, new Statement[]{node.getBody()});
	}

	@Override
	public boolean visit(TryStatement node) {
		List<CatchClause> catchs = (List<CatchClause>) node.catchClauses();
		Statement[] childs = new Statement[catchs.size()];
		for(int i=0; i<childs.length;i++)
			childs[i] = catchs.get(i).getBody();

		
		return visitNestedStruct(WeightFactors.tryFactor(), new Statement[]{node.getBody(), node.getFinally()}, childs);
	} 
	
	@Override
	public boolean visit(ConstructorInvocation node) {
		//these are  this() calls
		IMethodBinding mb = node.resolveConstructorBinding();
		return doVisitMethodInvocation(mb, WeightFactors.methodCallWeight());
	}
	
	@Override
	public boolean visit(SuperConstructorInvocation node) {
		IMethodBinding mb = node.resolveConstructorBinding();
		return doVisitMethodInvocation(mb, WeightFactors.superCallWeight());
	}
	
	public boolean visit(SuperMethodInvocation node) {
		IMethodBinding mb = node.resolveMethodBinding();
		return doVisitMethodInvocation(mb, WeightFactors.superCallWeight());
	}
	
	@Override
	public boolean visit(ClassInstanceCreation node) {
		IMethodBinding mb = node.resolveConstructorBinding();
		return doVisitMethodInvocation(mb, WeightFactors.methodCallWeight());
	}
	
	@Override
	public boolean visit(MethodInvocation node) {
		IMethodBinding mb = node.resolveMethodBinding();
		return doVisitMethodInvocation(mb, WeightFactors.methodCallWeight());
	}
		
	
		
	
	private boolean visitNestedStruct(BigInteger factor, ASTNode[] linearStatements, Statement[] nested) {
		WCCVisitor childVisitor = newChildVisitor();
		for(ASTNode s : linearStatements)
			if (s != null)
				s.accept(childVisitor); // for expressions might be null
		
		cost = cost.add(childVisitor.getCost());
		expr.append(" + (" + childVisitor.getCost() + ")");
				
		
		childVisitor = newChildVisitor();
		for(Statement child : nested)
			if (child != null)
				child.accept(childVisitor);  //else branch may be null
		BigInteger childCost = childVisitor.getCost();
		if (childCost.equals(BigInteger.valueOf(0)))
			childCost = BigInteger.valueOf(1); //we multiply, so nested cost can't be zero.  
		
		expr.append(" + " + factor + " * [" + childVisitor.getExpr() + "]");

		cost = cost.add(childCost.multiply(factor));
		        
		return false;
	}

	
	public boolean doVisitMethodInvocation(IMethodBinding mb, BigInteger invFactor) {
		if (mb.getDeclaringClass() ==
				this.currentMethod.md.resolveBinding().getDeclaringClass() ) {
			//local calls
			expr.append("+" +WeightFactors.methodCallWeight());
			cost = cost.add(WeightFactors.methodCallWeight());
			return true;
		} else {
			MethodSignature targetSignature = MethodSignature.from(mb);
			if (isUserDefined(targetSignature)) {
				if (stack.contains(targetSignature)) {
					for (int index = stack.indexOf(targetSignature); index < stack.size(); index++) {
						MethodSignature m = stack.get(index);
						MethodNode target = this.methodMap.get(m);
						target.setRecursive();
					}

					expr.append("+" +WeightFactors.recursiveCalllWeight());
					cost = cost.add(WeightFactors.recursiveCalllWeight());
				} else {
					stack.add(targetSignature);
					if (Modifier.isAbstract(mb.getModifiers())) {
						expr.append(" +").append(WeightFactors.methodCallWeight()).append("+");
						BigInteger abstractCost = abstractImplementationCost(mb);
						cost = cost.add(abstractCost.add(WeightFactors.methodCallWeight()));
					} else {
						
						BigInteger methodCost = methodCost(targetSignature);
						expr.append(" +").append(invFactor).
							append(" + [").append(methodCost).append("]");
						cost = cost.add(methodCost.add(invFactor));
					}
					MethodSignature pop = this.stack.pop();
					if (targetSignature != pop)
						System.out.println("Error!: ms: " + targetSignature + "  pop:" + pop);

				}
			}
		}
		return true;
	}
	
	private boolean isUserDefined(MethodSignature ms) {
		return this.methodMap.containsKey(ms);
	}
			
	
	private BigInteger abstractImplementationCost(IMethodBinding mb) {
		Set<MethodSignature> implementations = getImplementationsOf(mb);
		if (implementations.isEmpty()) {
			System.out.println("Warnning " + MethodSignature.from(mb) + " doesn't have implementations");
			return BigInteger.valueOf(0); //patological case
		} else {
			List<BigInteger> costs = new LinkedList<BigInteger>();
			for(MethodSignature impl : implementations) {
				if (stack.contains(impl)) {
					// recursive call to an abstract method  (composite-like pattern)
					// we consider only the cost of recursion factor
					costs.add(WeightFactors.recursiveCalllWeight());
				} else {
					stack.add(impl);
					costs.add(methodCost(impl));
					MethodSignature pop = this.stack.pop();
					if (impl != pop)
						System.out.println("Error!: ms: " + impl + "  pop:" + pop);
				}

			}
			return this.costModel.getMethodWeightStrategy().weight(costs, this.expr);
		}
	}

	private BigInteger methodCost(MethodSignature targetSignature) {
		MethodNode target = this.methodMap.get(targetSignature);
		BigInteger callCost = target.getCost(this.stack);
		return  callCost;
	}
	
		
	private WCCVisitor newChildVisitor() {
		return new WCCVisitor(this.currentMethod, this.costModel, this.methodMap, this.stack);
	}
	
	private Set<MethodSignature> getImplementationsOf(IMethodBinding mb) {
		try {
			return this.costModel.getDependencyModel().getImplementations(mb);
		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new HashSet<MethodSignature>();
		}
	}
	
}

