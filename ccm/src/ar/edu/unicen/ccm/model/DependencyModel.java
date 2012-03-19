package ar.edu.unicen.ccm.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.CycleDetector;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import ar.edu.unicen.ccm.bcs.MethodSignature;

public class DependencyModel {
	IJavaProject project;
	Collection<TypeDeclaration> types;
	Collection<MethodDeclaration> methods;
	DirectedGraph<MethodSignature, DefaultEdge> methodGraph;
	DirectedGraph<String, DefaultEdge> hierarchyGraph;
	
	Set<String> interfaces; 

	public DependencyModel(IJavaProject project) throws JavaModelException {
		this.project = project;
		
		buildModel();
	}
	
	public Collection<TypeDeclaration> getTypes() {
		return types;
	}
	public Collection<MethodDeclaration> getMethods() {
		return methods;
	}

	public Collection<String> getRootClasses() {
		List<String> root = new LinkedList<String>();
		for (String t : hierarchyGraph.vertexSet()) {
			if (!interfaces.contains(t)) { //is a class, not an interface 
				Set<DefaultEdge> incoming = hierarchyGraph.incomingEdgesOf(t);
				boolean isroot = true;
				for (DefaultEdge e : incoming) {
					String parent = hierarchyGraph.getEdgeSource(e);
					if (!interfaces.contains(parent)) { //type have a superclass
						isroot = false;
						break;
					}
				}
				if (isroot)
					root.add(t);
			}
		}
		return root;
	}

	public Set<MethodSignature> getRecursivePath(MethodSignature signature) {
		CycleDetector<MethodSignature, DefaultEdge> cd = new CycleDetector<MethodSignature, DefaultEdge>(
				this.methodGraph);
		return  cd.findCyclesContainingVertex(signature);
	}

	/**
	 * Get all subclasses of the given type
	 * @param type
	 * @return
	 */
	public Set<String> getAllSubtypes(String type) {
		Set<String> result = new HashSet<String>();
		for (String subType : getDirectSubtypes(type)) {
			result.add(subType);
			result.addAll(getAllSubtypes(subType));
		}
		return result;
	}
	
	/**
	 * Get all direct subclasses of the given type
	 * @param type
	 * @return
	 */
	public Set<String> getDirectSubtypes(String type) {
		Set<String> result = new HashSet<String>(); 
		for(DefaultEdge e : hierarchyGraph.outgoingEdgesOf(type)) {
			String subType = hierarchyGraph.getEdgeTarget(e);
			result.add(subType);
		}
		return result;
	}
	
	public Set<MethodSignature> getImplementations(IMethodBinding mb) {
		String type = mb.getDeclaringClass().getQualifiedName();
		Set<MethodSignature> result = new HashSet<MethodSignature>();
		for (String t : getAllSubtypes(type)) {
			MethodSignature signature = MethodSignature.from(t, mb.getName(),  mb.getParameterTypes());
			//All subtypes that had an implementation of this method.
			if (this.methodGraph.containsVertex(signature))
				result.add(signature);
			
		}
		return result;
	}
	

	private void buildModel() throws JavaModelException {
		this.types = extractTypes(project);
		this.methods = extractMethods(types);
		this.interfaces = new HashSet<String>();
		this.hierarchyGraph = extractHierarchyGraph(types);
		this.methodGraph = extractMethodGraph(methods);
	}

	private Collection<MethodDeclaration> extractMethods(Collection<TypeDeclaration> types) {
		List<MethodDeclaration> result = new LinkedList<MethodDeclaration>();
		for (TypeDeclaration t : types) {
			for (MethodDeclaration md : t.getMethods()) {
				result.add(md);
			}
		}
		return result;
	}
	
	private Collection<TypeDeclaration> extractTypes(IJavaProject project)
			throws JavaModelException {
		Collection<TypeDeclaration> types = new LinkedList<TypeDeclaration>();
		// find all declared types (including nested ones)
		for (IPackageFragment pk : project.getPackageFragments()) {
			if (pk.getKind() == IPackageFragmentRoot.K_SOURCE)
				for (ICompilationUnit unit : pk.getCompilationUnits()) {
					ASTParser parser = ASTParser.newParser(AST.JLS3);
					parser.setKind(ASTParser.K_COMPILATION_UNIT);
					parser.setSource(unit); // set source
					parser.setResolveBindings(true); // we need bindings later
														// on
					CompilationUnit cu = (CompilationUnit) parser
							.createAST(null /* IProgressMonitor */); // parse
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
	
	private DirectedGraph<String, DefaultEdge> extractHierarchyGraph(
			Collection<TypeDeclaration> td) {
		DirectedGraph<String, DefaultEdge> hierarchy = new DefaultDirectedGraph<String, DefaultEdge>(
				DefaultEdge.class);

		// create the hierarchy vertex
		for (TypeDeclaration t : types) {
			hierarchy.addVertex(t.resolveBinding().getQualifiedName());
			if (t.isInterface()) 
				interfaces.add(t.resolveBinding().getQualifiedName());
		}

		// TODO: interfaces not in this project ALSO must be addedd.. to find references
		// TODO: abstract classes not in this project ALSO must be added ..
		// to find implementations of a method.
		for (TypeDeclaration t : types) {
			ITypeBinding tb = t.resolveBinding();
			if (tb.getSuperclass() != null)
				if (hierarchy.containsVertex(tb.getSuperclass().getQualifiedName())) {
					hierarchy.addEdge(tb.getSuperclass().getQualifiedName(),
							tb.getQualifiedName());
				}
			for (ITypeBinding interfaz :  tb.getInterfaces()) {
				if (hierarchy.containsVertex(interfaz.getQualifiedName())) {
					hierarchy.addEdge(interfaz.getQualifiedName(),
							tb.getQualifiedName());
				}
			}
		}
		return hierarchy;
	}

	private DirectedGraph<MethodSignature, DefaultEdge> extractMethodGraph(Collection<MethodDeclaration> methods) {
		DirectedGraph<MethodSignature, DefaultEdge> methodGraph = new DefaultDirectedGraph<MethodSignature, DefaultEdge>(
				DefaultEdge.class);
		
		// populate the method graph with all its vertex
		for (MethodDeclaration md : methods) {
				MethodSignature signature = MethodSignature.from(md.resolveBinding());
				methodGraph.addVertex(signature);
		}
		
		/*
		for (MethodDeclaration md : methods) {
				MethodSignature signature = MethodSignature.from(md.resolveBinding());
				DependencyVisitor visitor = new DependencyVisitor(signature, methodGraph);
				if (md.getBody() != null) //TODO: abstract methods
					md.getBody().accept(visitor);
		}
		*/
		return methodGraph;
	}

}
