package ar.edu.unicen.ccm.utils;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
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
	
		
	public static String[] readFile(IFile file) {
		List<String> result = new LinkedList<String>();

		if (file.exists()) {
			try {
				InputStream fstream =file.getContents();
				// Get the object of DataInputStream
				DataInputStream in = new DataInputStream(fstream);
				BufferedReader br = new BufferedReader(new InputStreamReader(in));
				String strLine;
				//Read File Line By Line
				while ((strLine = br.readLine()) != null)   {
					result.add(strLine);
				}
				//Close the input stream
				in.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return result.toArray(new String[result.size()]);
	}
	
	
	
}
