package ar.edu.unicen.ccm.model.adapter;

import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

public class TypeDeclAdapter implements TypeAdapter {
	TypeDeclaration decl;
	
	public TypeDeclAdapter(TypeDeclaration decl) {
		this.decl = decl;
	}
	
	@Override
	public MethodDeclaration[] getMethods() {
		// TODO Auto-generated method stub
		return decl.getMethods();
	}

	@Override
	public ITypeBinding getBinding() {
		// TODO Auto-generated method stub
		return decl.resolveBinding();
	}

	@Override
	public String FQName() {
		return decl.resolveBinding().getBinaryName();
	}
	@Override
	public int fieldsCount() {
		return decl.getFields().length;
	}
	
	public boolean isClass() {
		return !this.decl.isInterface();
	}
	
	public ITypeBinding getSuperClass() {
		return this.decl.resolveBinding().getSuperclass();
	}
}
