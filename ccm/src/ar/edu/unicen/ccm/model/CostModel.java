package ar.edu.unicen.ccm.model;

import java.math.BigInteger;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import ar.edu.unicen.ccm.WeightFactors;
import ar.edu.unicen.ccm.bcs.MethodNode;
import ar.edu.unicen.ccm.bcs.MethodSignature;
import ar.edu.unicen.ccm.bcs.abstractmethod.AverageWeightStrategy;
import ar.edu.unicen.ccm.bcs.abstractmethod.MethodWeightStrategy;
import ar.edu.unicen.ccm.model.adapter.TypeAdapter;
import ar.edu.unicen.ccm.utils.Utils;

public class CostModel {

	DependencyModel dep;
	Map<MethodSignature, MethodNode> methodComplexity;
	
	Map<String, ClassComplexityInfo> weightedClassComplexity;
	
	private MethodWeightStrategy methodWeightStrategy;
	
	
	public CostModel(IJavaProject project) throws JavaModelException {
		this.dep = new DependencyModel(project);
		try {
			this.methodWeightStrategy = WeightFactors.methodWeightStrategy();
		} catch (Exception e) {
			this.methodWeightStrategy = new AverageWeightStrategy(); //default one
			e.printStackTrace();
		} 
		analyze();
	}
	
	public DependencyModel getDependencyModel() {
		return dep;
	}
	
	public Collection<TypeAdapter> getTypes() throws JavaModelException {
		return dep.getTypes();
	}
	
	public boolean isMethodInProject(MethodSignature signature) {
		return this.dep.methods.containsKey(signature);
	}
	
	private void analyze() throws JavaModelException {
		this.methodComplexity = new HashMap<MethodSignature, MethodNode>();
		this.weightedClassComplexity = new HashMap<String, ClassComplexityInfo>();
		for(MethodDeclaration md : dep.getMethods()) {
			MethodSignature signature = MethodSignature.from(md.resolveBinding());
			this.methodComplexity.put(signature,new MethodNode(md, this, this.methodComplexity));
		}
		
		for (TypeAdapter t : getTypes()) {
			this.weightedClassComplexity.put(t.FQName(),
					calculateWeightedClassComplexity(t));
		}
	}
	
	public ClassComplexityInfo getClassComplexityInfo(String type) {
		return this.weightedClassComplexity.get(type);
	}
	
	private ClassComplexityInfo calculateWeightedClassComplexity(TypeAdapter type) throws JavaModelException {
		
		//TypeDeclaration typeDecl = Utils.findType(typeHandle);
		
		Map<MethodSignature, MethodNode> methods = new HashMap<MethodSignature, MethodNode>();
		for (MethodDeclaration m : type.getMethods()) {
			MethodSignature signature = MethodSignature.from(m.resolveBinding());
			methods.put(signature,  getMethodComplexity(signature));
		}
		return new ClassComplexityInfo(type.FQName(),type.fieldsCount() , methods);
		
	}
	
	public HierarchyComplexityInfo hierarchyCostOf(String baseClass) {
		Set<String> subtypes;
		try {
			subtypes = this.dep.getDirectSubtypes(baseClass);
		} catch (JavaModelException e) {
			subtypes = new HashSet<String>();
			e.printStackTrace();
			//TODO: warn somehow
		}
		//If a class has "0" complexity, its hierarchy weight would be "0" too because
		//we multiply it. I think that's not the intended result, so here we force it to
		//1 on those cases.
		BigInteger baseWeight =  getClassComplexityInfo(baseClass).getWeightedClassComplexity().max(BigInteger.valueOf(1)); 
				
		if (subtypes.isEmpty()) {
			return new HierarchyComplexityInfo(baseWeight, 1, 1,  String.valueOf(baseWeight));
		}
		else {
			BigInteger childCost = BigInteger.valueOf(0);
			int max_depth = 1;
			int childClasses = 0;
			StringBuilder expressions = new StringBuilder();
			expressions.append("(");
			for (String subType : subtypes) { 
				HierarchyComplexityInfo childInfo = hierarchyCostOf(subType);
				childCost = childCost.add(childInfo.getCost());
				max_depth = Math.max(childInfo.getDepth(), max_depth);
				childClasses += childInfo.getClasses();
				expressions.append("+" + childInfo.getExpr());
			}
			expressions.append(")");
			
		
			return new HierarchyComplexityInfo(childCost.multiply(baseWeight), max_depth +1, childClasses +1, baseWeight + "*" + expressions.toString());
		}
	}
	
	public MethodNode getMethodComplexity(MethodSignature signature) {
		return methodComplexity.get(signature);
	}
	
	public MethodWeightStrategy getMethodWeightStrategy() {
		return methodWeightStrategy;
	}
		
}
