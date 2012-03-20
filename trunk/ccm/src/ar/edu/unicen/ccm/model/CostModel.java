package ar.edu.unicen.ccm.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import ar.edu.unicen.ccm.bcs.MethodNode;
import ar.edu.unicen.ccm.bcs.MethodSignature;
import ar.edu.unicen.ccm.utils.Utils;

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
	
	public Collection<IType> getTypes() throws JavaModelException {
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
			this.methodComplexity.put(signature,new MethodNode(md, dep, this.methodComplexity));
		}
		
		for (IType t : getTypes()) {
			this.weightedClassComplexity.put(t.getFullyQualifiedName('.'),
					calculateWeightedClassComplexity(t));
		}
	}
	
	public ClassComplexityInfo getClassComplexityInfo(String type) {
		return this.weightedClassComplexity.get(type);
	}
	
	private ClassComplexityInfo calculateWeightedClassComplexity(IType typeHandle) throws JavaModelException {
		
		TypeDeclaration typeDecl = Utils.findType(typeHandle);
		
		Map<MethodSignature, MethodNode> methods = new HashMap<MethodSignature, MethodNode>();
		for (MethodDeclaration m : typeDecl.getMethods()) {
			MethodSignature signature = MethodSignature.from(m.resolveBinding());
			methods.put(signature,  getMethodComplexity(signature));
		}
		return new ClassComplexityInfo(typeHandle.getFullyQualifiedName('.'),typeHandle.getFields().length , methods);
		
	}
	
	//TODO: count nesting level and total # of classes
	public int hierarchyCostOf(String baseClass) {
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
		int baseWeight = Math.max(
				getClassComplexityInfo(baseClass).getWeightedClassComplexity(), 
				1);
		if (subtypes.isEmpty())
			return baseWeight;
		else {
			int childCost = 0;
			for (String subType : subtypes)
				childCost += hierarchyCostOf(subType);
			return baseWeight * childCost;
		}
	}
	
	public MethodNode getMethodComplexity(MethodSignature signature) {
		return methodComplexity.get(signature);
	}
		
}
