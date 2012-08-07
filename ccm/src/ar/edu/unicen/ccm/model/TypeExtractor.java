package ar.edu.unicen.ccm.model;

import java.util.Collection;
import java.util.LinkedList;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import ar.edu.unicen.ccm.model.adapter.AnonymousTypeAdapter;
import ar.edu.unicen.ccm.model.adapter.TypeAdapter;
import ar.edu.unicen.ccm.model.adapter.TypeDeclAdapter;

public class TypeExtractor extends ASTVisitor {

	private Collection<TypeAdapter> types;
	private TypeDeclaration lastType;
	private int lastAnon;
	
	
	public TypeExtractor() {
		types = new LinkedList<TypeAdapter>();
	}
	
	@Override
	public boolean visit(AnonymousClassDeclaration node) {
		// TODO Auto-generated method stub
		types.add(new AnonymousTypeAdapter(node, lastType, lastAnon));
		lastAnon++;
		return super.visit(node);
	}
	
	@Override
	public boolean visit(TypeDeclaration node) {
		types.add(new TypeDeclAdapter(node));
		this.lastAnon = 1;
		this.lastType = node;
		return super.visit(node);
	}
	
	public Collection<TypeAdapter> getTypes() {
		return types;
	}
	
	
}
