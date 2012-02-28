package ar.edu.unicen.ccm.bcs;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

import ar.edu.unicen.ccm.utils.MapFun;
import ar.edu.unicen.ccm.utils.Utils;


public class MethodSignature {
	
	public static String from(IMethodBinding mb) {
		String clazz = mb.getDeclaringClass().getQualifiedName();
		String name = mb.getName();
		Object[] params = Utils.map(mb.getParameterTypes(), new MapFun() {
			public Object map(Object item) {
				ITypeBinding tb = (ITypeBinding) item;
				return tb.getQualifiedName();
			}
		});
		return calculateId(clazz, name, params);
	}
	public static String from(String clazz, String name, ITypeBinding[] parameters) {
		Object[] params = Utils.map(parameters, new MapFun() {
			public Object map(Object item) {
				ITypeBinding tb = (ITypeBinding) item;
				return tb.getQualifiedName();
			}
		});
		return calculateId(clazz, name, params);
	}
	
	private static String calculateId(String clazz, String name, Object[] arguments) {
		StringBuilder b = new StringBuilder(clazz);
		b.append(".").append(name).append("(");
		b.append(Utils.join(arguments, ","));
		return b.append(")").toString();
	}
	
	

	
}
