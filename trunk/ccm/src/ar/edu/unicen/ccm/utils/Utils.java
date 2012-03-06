package ar.edu.unicen.ccm.utils;

import java.lang.reflect.Array;


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
}
