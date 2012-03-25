package ar.edu.unicen.ccm;

import java.math.BigInteger;

import ar.edu.unicen.ccm.bcs.abstractmethod.MethodWeightStrategy;
import ar.edu.unicen.ccm.ui.PreferenceConstants;

/**
 * These are small constants,  but since BigInteger methods used elsewhere requires
 * other BigIntegers as arguments,  it is simpler to return BigInteger from here than
 * to do the conversion on every place needed.
 * @author pablo
 *
 */
public class WeightFactors {
	
	public static BigInteger conditionalFactor() {
		return get(PreferenceConstants.P_CONDITIONAL_FACTOR);
	}
	
	public static BigInteger loopFactor() {
		return get(PreferenceConstants.P_LOOP_FACTOR);
	}
	
	public static BigInteger switchFactor() {
		return get(PreferenceConstants.P_SWITCH_FACTOR);
	}
	public static BigInteger methodCallWeight() {
		return get(PreferenceConstants.P_METHOD_CALL_WEIGHT);
	}
	public static BigInteger recursiveCalllWeight() {
		return get(PreferenceConstants.P_RECURSIVE_CALL_WEIGHT);
	}
	public static BigInteger superCallWeight() {
		return get(PreferenceConstants.P_SUPER_CALL_WEIGHT);
	}
	public static BigInteger sequenceWeight() {
		return get(PreferenceConstants.P_SEQUENCE_WEIGHT);
	}
	
	public static BigInteger tryFactor() {
		return get(PreferenceConstants.P_TRY_FACTOR);
	}
	
	private static BigInteger get(String key) {
		return BigInteger.valueOf(Activator.getDefault().getPreferenceStore().getInt(key));
	}
	
	
	public static MethodWeightStrategy methodWeightStrategy() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		String clazz = Activator.getDefault().getPreferenceStore().getString(PreferenceConstants.P_STRATEGY);
		return (MethodWeightStrategy)Class.forName(clazz).newInstance();
	}

	
	

}
