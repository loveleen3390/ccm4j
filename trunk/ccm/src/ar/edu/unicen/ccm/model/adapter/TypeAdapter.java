package ar.edu.unicen.ccm.model.adapter;

import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;

/**
 * Adapts TypeDeclaration and AnonymousClassDeclaration to a unified interface
 * @author pablo
 *
 */
public interface TypeAdapter {
	MethodDeclaration[] getMethods();
	ITypeBinding getBinding();
	String FQName();
	int fieldsCount();
	boolean isClass();
	ITypeBinding getSuperClass();
}
