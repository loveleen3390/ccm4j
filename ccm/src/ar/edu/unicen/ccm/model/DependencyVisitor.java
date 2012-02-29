package ar.edu.unicen.ccm.model;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import ar.edu.unicen.ccm.bcs.MethodSignature;

public class DependencyVisitor extends ASTVisitor {
	private DirectedGraph<MethodSignature, DefaultEdge> graph;
	private MethodSignature mySignature;
	public DependencyVisitor(MethodSignature mySignature, DirectedGraph<MethodSignature, DefaultEdge> graph) {
		this.graph = graph;
		this.mySignature = mySignature;
	}
	@Override
	public boolean visit(MethodInvocation mi) {
		MethodSignature invokedSignature = MethodSignature.from(mi.resolveMethodBinding());
		if (graph.containsVertex(invokedSignature)) {
			//Otherwise it is a library api
			this.graph.addEdge(this.mySignature, invokedSignature);
		}
		return super.visit(mi);
	}
}


