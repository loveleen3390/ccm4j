package ar.edu.unicen.ccm.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IRegion;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import ar.edu.unicen.ccm.bcs.MethodSignature;
import ar.edu.unicen.ccm.utils.Utils;

public class DependencyModel {
	IJavaProject project;
	Collection<TypeDeclaration> types;
	Map<MethodSignature, MethodDeclaration> methods;
	Set<String> interfaces; 
	ITypeHierarchy typeHierarchy;
	Set<IPackageFragment> packages;
	String[] rules;
	
	public DependencyModel(IJavaProject project) throws JavaModelException {
		this.project = project;
		IRegion region = JavaCore.newRegion();
		this.packages = new HashSet<IPackageFragment>();
		
		 
		this.rules = Utils.readFile(project.getProject().getFile("ccm4j.packages"));
		
		for (IPackageFragment pk : project.getPackageFragments()) {
			if (pk.getKind() == IPackageFragmentRoot.K_SOURCE) {
				if (isPkIncluded(pk)) {
					region.add(pk);
					packages.add(pk);
				}
			}
		}
		this.typeHierarchy = project.newTypeHierarchy(region, null);
		
		 
		
		buildModel();
	}
	private boolean isPkIncluded(IPackageFragment pk) {
		for (String rule : this.rules ) {
			if (rule.equals(pk.getElementName())) 
				return true;
		}
		return rules.length == 0; //if not specified, include all by default
	}
	

	public Collection<IType> getTypes() throws JavaModelException {
		Collection<IType> r = new LinkedList<IType>();
		// The typeHierarchy contains superclasses not in this project, for example
		// Object
		for (IType t : this.typeHierarchy.getAllClasses())
			if (isClassInScope(t))
				r.add(t);
		return r;
	}
	public Collection<MethodDeclaration> getMethods() {
		return methods.values();
	}

	private boolean isClassInScope(IType t) throws JavaModelException {
		IPackageFragment p = t.getPackageFragment();
		// TODO: we don't handle anonymous defined classes yet
		return this.packages.contains(p) && !t.isAnonymous();
	}
	public Collection<String> getRootClasses() throws JavaModelException {
		List<String> root = new LinkedList<String>();
		for (IType c : this.typeHierarchy.getAllClasses()) {
			IType superClass = this.typeHierarchy.getSuperclass(c);
			if (isClassInScope(c) && !isClassInScope(superClass)) {
				root.add(c.getFullyQualifiedName('.'));
			} 
		}
		return root;
	}

	
	/**
	 * Get all subclasses of the given type
	 * @param type
	 * @return
	 */
	public IType[] getAllSubtypes(String type) throws JavaModelException {
		IType superType = this.project.findType(type); //maybe interface or class..
		Collection<IType> result = new LinkedList<IType>();
		for (IType sub : this.typeHierarchy.getAllSubtypes(superType)) {
			if (isClassInScope(sub))
				result.add(sub);
		}
		IType[] r=	new IType[result.size()];
		return result.toArray(r);
	}
	
	/**
	 * Get all direct subclasses of the given type
	 * @param type
	 * @return
	 * @throws JavaModelException 
	 */
	public Set<String> getDirectSubtypes(String type) throws JavaModelException {
		Set<String> result = new HashSet<String>();
		IType superType = this.project.findType(type); //maybe interface or class..
		for (IType sub : this.typeHierarchy.getSubtypes(superType)) {
			if (isClassInScope(sub))
				result.add(sub.getFullyQualifiedName('.'));
		}
		return result;
	}
	
	public Set<MethodSignature> getImplementations(IMethodBinding mb) throws JavaModelException {
		String type = mb.getDeclaringClass().getQualifiedName();
		Set<MethodSignature> result = new HashSet<MethodSignature>();
		for (IType t : getAllSubtypes(type)) {
			MethodSignature signature = MethodSignature.from(t.getFullyQualifiedName('.'), mb.getName(),  mb.getParameterTypes());
			//All subtypes that had an implementation of this method.
			//TODO: I didn't find a reasonable way to figure out the REAL implementations,
			//      that is, classes that DO define the method, and not only inherit it from
			//      some superclass (at least not starting from an IType). So for that reason
			//      we still need to construct the method map in advance, with lot of overhead
			//      Once I fix this,  I think we should be able to do the analysis in streaming
			//		reducing memory and cpu consuption
			if (this.methods.containsKey(signature))
				result.add(signature);
		}
		return result;
	}
	

	private void buildModel() throws JavaModelException {
		this.types = extractTypes(project);
		this.methods = extractMethods(types);
	}

	private Map<MethodSignature, MethodDeclaration> extractMethods(Collection<TypeDeclaration> types) {
		Map<MethodSignature,MethodDeclaration> result = new HashMap<MethodSignature,MethodDeclaration>();
		for (TypeDeclaration t : types) {
			
			for (MethodDeclaration md : t.getMethods()) {
				result.put(MethodSignature.from(md.resolveBinding()), md);
			}
		}
		return result;
	}
	
	private Collection<TypeDeclaration> extractTypes(IJavaProject project)
			throws JavaModelException {
		Collection<TypeDeclaration> types = new LinkedList<TypeDeclaration>();
		// find all declared types (including nested ones)
		for (IPackageFragment pk : this.packages) {
			if (pk.getKind() == IPackageFragmentRoot.K_SOURCE)
				for (ICompilationUnit unit : pk.getCompilationUnits()) {
					ASTParser parser = ASTParser.newParser(AST.JLS3);
					parser.setKind(ASTParser.K_COMPILATION_UNIT);
					parser.setSource(unit); // set source
					parser.setResolveBindings(true); // we need bindings later
														// on
					CompilationUnit cu = (CompilationUnit) parser
							.createAST(null ); // IProgressMonitor 
					extractTypesFromCU(cu, types);
				}
		}
		return types;
	}


	private void extractTypesFromCU(CompilationUnit cu, Collection<TypeDeclaration> types) {
		for (AbstractTypeDeclaration t : (List<AbstractTypeDeclaration>) cu.types()) {
			extractTypesRecursive(t, types);
		}
	}
	private void extractTypesRecursive(AbstractTypeDeclaration t, Collection<TypeDeclaration> types) {
		if (t.getNodeType() == AbstractTypeDeclaration.TYPE_DECLARATION) {
			TypeDeclaration td = (TypeDeclaration) t;
			types.add((TypeDeclaration) t);
			for (TypeDeclaration childType : td.getTypes())
				extractTypesRecursive(childType, types);
		}
	}

}
