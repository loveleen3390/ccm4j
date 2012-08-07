package ar.edu.unicen.ccm.bcs;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

import ar.edu.unicen.ccm.utils.MapFun;
import ar.edu.unicen.ccm.utils.Utils;


public class MethodSignature {
	
	private String signature;

	// only a String,  not really matters, it is opaque, just to use as 
	// a key in Maps and Graphs.  
	// Use a distinctive object class instead of the generic "String" to make
	// its usage explicit
	private MethodSignature(String signature) {
		this.signature = signature;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof MethodSignature)
			return signature.equals(((MethodSignature)obj).signature);
		return false;
	}
	@Override
	public int hashCode() {
		return signature.hashCode();
	}
	@Override
	public String toString() {
		return signature;
	}
	
	public static MethodSignature from(IMethodBinding mb) {
		String clazz = mb.getDeclaringClass().getBinaryName();
		String name = mb.getName();
		Object[] params = Utils.map(mb.getParameterTypes(), new MapFun() {
			public Object map(Object item) {
				ITypeBinding tb = (ITypeBinding) item;
				return tb.getBinaryName();
			}
		});
		return calculateId(clazz, name, params);
	}
	
	public static MethodSignature from(String clazz, String name, ITypeBinding[] parameters) {
		Object[] params = Utils.map(parameters, new MapFun() {
			public Object map(Object item) {
				ITypeBinding tb = (ITypeBinding) item;
				return tb.getBinaryName();
			}
		});
		return calculateId(clazz, name, params);
	}
	
	private static MethodSignature calculateId(String clazz, String name, Object[] arguments) {
		StringBuilder b = new StringBuilder(clazz);
		b.append(".").append(name).append("(");
		b.append(Utils.join(arguments, ","));
		return new MethodSignature(b.append(")").toString());
	}
	
	

	
}
