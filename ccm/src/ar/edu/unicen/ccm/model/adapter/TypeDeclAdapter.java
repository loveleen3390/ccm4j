package ar.edu.unicen.ccm.model.adapter;

import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

public class TypeDeclAdapter implements TypeAdapter {
	TypeDeclaration decl;
	ITypeBinding binding;
	String fqName;
	
	public TypeDeclAdapter(TypeDeclaration decl) {
		this.decl = decl;
		this.binding = decl.resolveBinding();
		this.fqName = binding.getBinaryName();
	}
	
	@Override
	public MethodDeclaration[] getMethods() {
		// TODO Auto-generated method stub
		return decl.getMethods();
	}

@Override
	public ITypeBinding getBinding() {
		return binding;
	}	

	@Override
	public String FQName() {
		return fqName;
	}
	@Override
	public int fieldsCount() {
		return decl.getFields().length;
	}
	
	public boolean isClass() {
		return !this.decl.isInterface();
	}
	
	public ITypeBinding getSuperClass() {
		return getBinding().getSuperclass();
	}
}
