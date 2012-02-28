package ar.edu.unicen.ccm;

import ar.edu.unicen.ccm.ui.PreferenceConstants;

public class WeightFactors {
	
	public static int conditionalFactor() {
		return get(PreferenceConstants.P_CONDITIONAL_FACTOR);
	}
	
	public static int loopFactor() {
		return get(PreferenceConstants.P_LOOP_FACTOR);
	}
	
	public static int switchFactor() {
		return get(PreferenceConstants.P_SWITCH_FACTOR);
	}
	public static int methodCallWeight() {
		return get(PreferenceConstants.P_METHOD_CALL_WEIGHT);
	}
	public static int recursiveCalllWeight() {
		return get(PreferenceConstants.P_RECURSIVE_CALL_WEIGHT);
	}
	public static int sequenceWeight() {
		return get(PreferenceConstants.P_SEQUENCE_WEIGHT);
	}
	
	private static int get(String key) {
		return Activator.getDefault().getPreferenceStore().getInt(key);
	}
	

}
