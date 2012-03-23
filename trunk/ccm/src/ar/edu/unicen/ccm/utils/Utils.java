package ar.edu.unicen.ccm.utils;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.internal.core.SourceType;




public class Utils {

	public static String join(Object[] s, String delimiter) {
	     StringBuilder builder = new StringBuilder();
	     for(int i=0;i<s.length;i++) {
	         builder.append(s[i]);
	         if (i +1 < s.length) 
	        	 builder.append(delimiter);
	     }
	     return builder.toString();
	 }
	
	public static Object[] map(Object[] array, MapFun f) {
		Object[] result = (Object[])Array.newInstance(Object.class, array.length);
		for (int i=0;i<array.length;i++)
			result[i] = f.map(array[i]);
		return result;
	}
	
	/* TODO: It won't work with anonymous classes.  
	 * TODO: really inneficient, we are parsing the file again..
	 */
	public static TypeDeclaration findType(IType typeHandle) {
		IJavaElement parent = typeHandle.getParent();
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(typeHandle.getCompilationUnit()); // set source
		parser.setResolveBindings(true); // we need bindings later
											// on
		CompilationUnit cu = (CompilationUnit) parser
				.createAST(null); // parse
		return findTypeInCU(cu, typeHandle);
    }

	 
	private static TypeDeclaration findTypeInCU(CompilationUnit cu, IType typeHandle) {
		Collection<TypeDeclaration> typesInCu = new LinkedList<TypeDeclaration>();
		extractTypesFromCU(cu, typesInCu);
		//TODO: buscar tambien en las clases anonimas..
  	    int occurenceCount = ((SourceType)typeHandle).occurrenceCount;
  	    boolean searchAnonymous =  typeHandle.getElementName().length() == 0;

		for (TypeDeclaration t : typesInCu) {
			String qn = t.resolveBinding().getQualifiedName();
			if (qn.equals(typeHandle.getFullyQualifiedName('.')))
				return	t;
		}
		return null;
	}
	

	private static void extractTypesFromCU(CompilationUnit cu, Collection<TypeDeclaration> types) {
		for (AbstractTypeDeclaration t : (List<AbstractTypeDeclaration>) cu.types()) {
			extractTypesRecursive(t, types);
		}
	}
	private static void extractTypesRecursive(AbstractTypeDeclaration t, Collection<TypeDeclaration> types) {
		if (t.getNodeType() == AbstractTypeDeclaration.TYPE_DECLARATION) {
			TypeDeclaration td = (TypeDeclaration) t;
			types.add(td);
			for (TypeDeclaration childType : td.getTypes())
				extractTypesRecursive(childType, types);
		}
	}
	
	
}
