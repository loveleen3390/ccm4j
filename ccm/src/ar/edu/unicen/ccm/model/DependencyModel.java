package ar.edu.unicen.ccm.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

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
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import ar.edu.unicen.ccm.bcs.MethodSignature;
import ar.edu.unicen.ccm.model.adapter.TypeAdapter;
import ar.edu.unicen.ccm.model.adapter.TypeDeclAdapter;
import ar.edu.unicen.ccm.utils.Utils;

public class DependencyModel {
	IJavaProject project;
	Collection<TypeAdapter> types;
	Map<MethodSignature, MethodDeclaration> methods;
	Map<String, Set<String>> hierarchy;
	
	Set<String> interfaces; 
	ITypeHierarchy typeHierarchy;
	Set<IPackageFragment> packages;
	Set<String> packageStrings;
	String[] rules;
	
	public DependencyModel(IJavaProject project) throws JavaModelException {
		this.project = project;
		IRegion region = JavaCore.newRegion();
		this.packages = new HashSet<IPackageFragment>();
		this.packageStrings = new HashSet<String>();
		this.hierarchy = new HashMap<String,Set<String>>();
		
		 
		this.rules = Utils.readFile(project.getProject().getFile("ccm4j.packages"));
		
		for (IPackageFragment pk : project.getPackageFragments()) {
			if (pk.getKind() == IPackageFragmentRoot.K_SOURCE) {
				if (isPkIncluded(pk)) {
					region.add(pk);
					packages.add(pk);
					packageStrings.add(pk.getElementName());
				}
			}
		}
		this.typeHierarchy = project.newTypeHierarchy(region, null);
		buildModel();
	}
	private boolean isPkIncluded(IPackageFragment pk) {
		for (String rule : this.rules ) {
			if (pk.getElementName().startsWith(rule)) 
				return true;
		}
		return rules.length == 0; //if not specified, include all by default
	}
	

	public Collection<TypeAdapter> getTypes() throws JavaModelException {
		return this.types;
	}
	public Collection<MethodDeclaration> getMethods() {
		return methods.values();
	}

	private boolean isClassInScope(ITypeBinding t) {
		return this.packageStrings.contains(t.getPackage().getName());
	}
	
	private boolean isClassInScope(IType t) throws JavaModelException {
		IPackageFragment p = t.getPackageFragment();
		return this.packages.contains(p) ;
	}
	public Collection<String> getRootClasses() throws JavaModelException {
		List<String> root = new LinkedList<String>();
		for (TypeAdapter ta : this.getTypes()) {
			if (ta.isClass()) { 
				ITypeBinding superClazz =	ta.getSuperClass();
				if (! isClassInScope(superClazz))
					root.add(ta.FQName());
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
		/*Set<String> result = new HashSet<String>();
		IType superType = this.project.findType(type); //maybe interface or class..
		for (IType sub : this.typeHierarchy.getSubtypes(superType)) {
			if (isClassInScope(sub))
				result.add(sub.getFullyQualifiedName('$'));
		}
		*/
		/*
		// here seems to be a bug in eclipse.. if the subtype is anonymous,
		// it allways returns the first anonymous class of the enclosing class,
		// rather than the correct one.
		// Example,  if the subclass is  a.b.C$2 , it will return a.b.C$1 anyway
		 * For that reason we had our own hierarchy tree
		*/
		Set<String> result = this.hierarchy.get(type);
		if (result == null)
			result = Collections.EMPTY_SET;
		return result;
	}
	
	
	//TODO: see comment on getDirectSubtypes. That bug might impact here too.
	public Set<MethodSignature> getImplementations(IMethodBinding mb) throws JavaModelException {
		String type = mb.getDeclaringClass().getBinaryName();
		Set<MethodSignature> result = new HashSet<MethodSignature>();
		IType[] subtypes = getAllSubtypes(type);
		for (IType t : subtypes) {
			
			//All subtypes that had an implementation of this method.
			//TODO: I didn't find a reasonable way to figure out the REAL implementations,
			//      that is, classes that DO define the method, and not only inherit it from
			//      some superclass (at least not starting from an IType). So for that reason
			//      we still need to construct the method map in advance, with lot of overhead
			//      Once I fix this,  I think we should be able to do the analysis in streaming
			//		reducing memory and cpu consuption
			IType toCheck = t;
			while((toCheck != null) && isClassInScope(toCheck)) {
				MethodSignature signature = MethodSignature.from(toCheck.getFullyQualifiedName('$'), mb.getName(),  mb.getParameterTypes());
				if (this.methods.containsKey(signature)) {
					result.add(signature);
					break;
				}
				toCheck =  this.typeHierarchy.getSuperclass(toCheck);
			}

		}
		return result;
	}
	

	private void buildModel() throws JavaModelException {
		this.types = extractTypes(project);
		buildHierarchy();
		this.methods = extractMethods(types);
	}

	private Map<MethodSignature, MethodDeclaration> extractMethods(Collection<TypeAdapter> types) {
		Map<MethodSignature,MethodDeclaration> result = new HashMap<MethodSignature,MethodDeclaration>();
		for (TypeAdapter t : types) {
			for (MethodDeclaration md : t.getMethods()) {
				result.put(MethodSignature.from(md.resolveBinding()), md);
			}
		}
		return result;
	}
	
	private Collection<TypeAdapter> extractTypes(IJavaProject project)
			throws JavaModelException {
		TypeExtractor visitor = new TypeExtractor();
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
					cu.accept(visitor);
				}
		}
		return visitor.getTypes();
	}


	private void buildHierarchy() {
		for (TypeAdapter t : this.types) {
			if (t.isClass())
				registerSubclass(t.getSuperClass().getBinaryName(), t.FQName());
		}
	}
	private void registerSubclass(String superClass, String sub) {
		Set<String> childs = this.hierarchy.get(superClass);
		if (childs == null) {
			childs = new TreeSet<String>();
			this.hierarchy.put(superClass, childs);
		}
		childs.add(sub);
	}

}
