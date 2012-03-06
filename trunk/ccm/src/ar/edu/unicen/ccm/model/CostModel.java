package ar.edu.unicen.ccm.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import ar.edu.unicen.ccm.bcs.MethodNode;
import ar.edu.unicen.ccm.bcs.MethodSignature;

public class CostModel {

	DependencyModel dep;
	Map<MethodSignature, MethodNode> methodComplexity;
	
	Map<String, ClassComplexityInfo> weightedClassComplexity;
	
	public CostModel(IJavaProject project) throws JavaModelException {
		this.dep = new DependencyModel(project);
		analyze();
	}
	
	public DependencyModel getDependencyModel() {
		return dep;
	}
	
	public Collection<TypeDeclaration> getTypes() {
		return dep.getTypes();
	}
	
	public boolean isMethodInProject(MethodSignature signature) {
		return this.dep.methodGraph.containsVertex(signature);
	}
	
	private void analyze() {
		this.methodComplexity = new HashMap<MethodSignature, MethodNode>();
		this.weightedClassComplexity = new HashMap<String, ClassComplexityInfo>();
		for(MethodDeclaration md : dep.getMethods()) {
			MethodSignature signature = MethodSignature.from(md.resolveBinding());
			this.methodComplexity.put(signature,new MethodNode(md, dep, this.methodComplexity));
		}
		
		for (TypeDeclaration t : getTypes()) {
			this.weightedClassComplexity.put(t.resolveBinding().getQualifiedName(),
					calculateWeightedClassComplexity(t));
		}
	}
	
	public ClassComplexityInfo getClassComplexityInfo(String type) {
		return this.weightedClassComplexity.get(type);
	}
	private ClassComplexityInfo calculateWeightedClassComplexity(TypeDeclaration t) {
		ITypeBinding tb = t.resolveBinding();
		
		Map<MethodSignature, MethodNode> methods = new HashMap<MethodSignature, MethodNode>();
		for (MethodDeclaration m : t.getMethods()) {
			MethodSignature signature = MethodSignature.from(m.resolveBinding());
			methods.put(signature,  getMethodComplexity(signature));
		}
		return new ClassComplexityInfo(tb.getQualifiedName(),t.getFields().length , methods);
		
	}
	
	public int hierarchyCostOf(String baseClass) {
		Set<String> subtypes = this.dep.getDirectSubtypes(baseClass);
		if (subtypes.isEmpty())
			return getClassComplexityInfo(baseClass).getWeightedClassComplexity();
		else {
			int childCost = 0;
			for (String subType : subtypes)
				childCost += hierarchyCostOf(subType);
			return getClassComplexityInfo(baseClass).getWeightedClassComplexity() * childCost;
		}
	}
	
	
	
	public MethodNode getMethodComplexity(MethodSignature signature) {
		return methodComplexity.get(signature);
	}
	
	
	
}
