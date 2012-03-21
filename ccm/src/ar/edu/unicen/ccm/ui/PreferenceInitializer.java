package ar.edu.unicen.ccm.ui;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import ar.edu.unicen.ccm.Activator;


/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setDefault(PreferenceConstants.P_CONDITIONAL_FACTOR, 2);
		store.setDefault(PreferenceConstants.P_LOOP_FACTOR, 3);
		store.setDefault(PreferenceConstants.P_METHOD_CALL_WEIGHT, 2);
		store.setDefault(PreferenceConstants.P_RECURSIVE_CALL_WEIGHT, 5);
		store.setDefault(PreferenceConstants.P_SUPER_CALL_WEIGHT, 2);
		store.setDefault(PreferenceConstants.P_SEQUENCE_WEIGHT, 1);
		store.setDefault(PreferenceConstants.P_SWITCH_FACTOR, 3);
		store.setDefault(PreferenceConstants.P_TRY_FACTOR, 3);
	}

}
