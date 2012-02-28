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
	DirectedGraph<String, DefaultEdge> methodGraph;
	DirectedGraph<String, DefaultEdge> hierarchyGraph;

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
			if (hierarchyGraph.inDegreeOf(t) == 0) {
				// TODO: que no sea interfaz..
				// it is not subclass of any class in this project
				root.add(t);
			}
		}
		return root;
	}

	public Set<String> getRecursivePath(String signature) {
		CycleDetector<String, DefaultEdge> cd = new CycleDetector<String, DefaultEdge>(
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
	
	public Set<String> getImplementations(IMethodBinding mb) {
		String type = mb.getDeclaringClass().getQualifiedName();
		Set<String> result = new HashSet<String>();
		for (String t : getAllSubtypes(type)) {
			String signature = MethodSignature.from(t, mb.getName(),  mb.getParameterTypes());
			//All subtypes that had an implementation of this method.
			if (this.methodGraph.containsVertex(signature))
				result.add(signature);
			
		}
		return result;
	}
	

	private void buildModel() throws JavaModelException {
		this.types = extractTypes(project);
		this.methods = extractMethods(types);
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
					for (AbstractTypeDeclaration t : (List<AbstractTypeDeclaration>) cu
							.types()) {
						// TODO: recursion: add types recursive, type inside type inside type
						if (t.getNodeType() == AbstractTypeDeclaration.TYPE_DECLARATION) {
							TypeDeclaration td = (TypeDeclaration) t;
							types.add((TypeDeclaration) t);
							for (TypeDeclaration childType : td.getTypes())
								types.add(childType);
						}
					}
				}
		}
		return types;
	}

	private DirectedGraph<String, DefaultEdge> extractHierarchyGraph(
			Collection<TypeDeclaration> td) {
		DirectedGraph<String, DefaultEdge> hierarchy = new DefaultDirectedGraph<String, DefaultEdge>(
				DefaultEdge.class);

		// create the hierarchy vertex
		for (TypeDeclaration t : types) {
			if (!t.isInterface()) {// TODO: agregar las interfaces
				hierarchy.addVertex(t.resolveBinding().getQualifiedName());
			}
		}
		// create the links TODO: add interfaces
		// TODO: interfaces not in this project ALSO must be addedd.. to find references
		// TODO: abstract classes not in this project ALSO must be added ..
		// to find implementations of a method.
		// when looking for roots, one must search the ones that don't have
		// a parent class in this project.
		for (TypeDeclaration t : types) {
			ITypeBinding tb = t.resolveBinding();
			if (hierarchy.containsVertex(tb.getSuperclass().getQualifiedName())) {
				hierarchy.addEdge(tb.getSuperclass().getQualifiedName(),
						tb.getQualifiedName());
			}
		}
		return hierarchy;
	}

	private DirectedGraph<String, DefaultEdge> extractMethodGraph(Collection<MethodDeclaration> methods) {
		DirectedGraph<String, DefaultEdge> methodGraph = new DefaultDirectedGraph<String, DefaultEdge>(
				DefaultEdge.class);
		
		// populate the method graph with all its vertex
		for (MethodDeclaration md : methods) {
				String signature = MethodSignature.from(md.resolveBinding());
				methodGraph.addVertex(signature);
		}
		
		
		for (MethodDeclaration md : methods) {
				String signature = MethodSignature.from(md.resolveBinding());
				DependencyVisitor visitor = new DependencyVisitor(signature, methodGraph);
				md.getBody().accept(visitor);
		}
		return methodGraph;
	}

}
