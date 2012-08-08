package ar.edu.unicen.ccm.model.adapter;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

public class AnonymousTypeAdapter implements TypeAdapter {
	AnonymousClassDeclaration clazz;
	String FQName;
	ITypeBinding binding;
	
	

	public AnonymousTypeAdapter(AnonymousClassDeclaration clazz, TypeDeclaration parent, int pos) {
		this.clazz = clazz;
		//this.FQName = parent.resolveBinding().getQualifiedName() + "$" + pos;
		this.FQName = clazz.resolveBinding().getBinaryName();
		this.binding = clazz.resolveBinding();
	}
	
	@Override
	public MethodDeclaration[] getMethods() {
		ArrayList<MethodDeclaration> results = new ArrayList<MethodDeclaration>();
		for (BodyDeclaration bd : ((List<BodyDeclaration>)clazz.bodyDeclarations())) 
			if (bd.getNodeType() == BodyDeclaration.METHOD_DECLARATION)
				results.add((MethodDeclaration)bd);
		return results.toArray(new MethodDeclaration[results.size()]);
	}

	@Override
	public ITypeBinding getBinding() {
		return binding;
	}
	
	public String FQName() {
		return this.FQName;
	}
	@Override
	public int fieldsCount() {
		int count =0;
		for (BodyDeclaration bd : ((List<BodyDeclaration>)clazz.bodyDeclarations())) 
			if (bd.getNodeType() == BodyDeclaration.FIELD_DECLARATION)
				count++;
		return count;
	}

	@Override
	public boolean isClass() {
		return true;
	}
	
	public ITypeBinding getSuperClass() {
		return this.getBinding().getSuperclass();
	}
}
