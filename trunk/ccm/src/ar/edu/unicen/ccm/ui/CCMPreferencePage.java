package ar.edu.unicen.ccm.ui;

import org.eclipse.jface.preference.*;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbench;

import ar.edu.unicen.ccm.Activator;
import ar.edu.unicen.ccm.bcs.abstractmethod.AverageWeightStrategy;
import ar.edu.unicen.ccm.bcs.abstractmethod.MaxWeightStrategy;
import ar.edu.unicen.ccm.bcs.abstractmethod.MinWeightStrategy;
import ar.edu.unicen.ccm.bcs.abstractmethod.SumWeightStrategy;


public class CCMPreferencePage
	extends FieldEditorPreferencePage
	implements IWorkbenchPreferencePage {

	public CCMPreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("Cognitive Complexity Metrics settings");
	}
	
	/**
	 * Creates the field editors. Field editors are abstractions of
	 * the common GUI blocks needed to manipulate various types
	 * of preferences. Each field editor knows how to save and
	 * restore itself.
	 */
	public void createFieldEditors() {
	
		addField(new IntegerFieldEditor(
				PreferenceConstants.P_SEQUENCE_WEIGHT, 
				"Sequence BCS weight:", 
				getFieldEditorParent()));
		addField(new IntegerFieldEditor(
				PreferenceConstants.P_CONDITIONAL_FACTOR, 
				"Call to a method m inside condition (C) weigth:", 
				getFieldEditorParent()));
		addField(new IntegerFieldEditor(
				PreferenceConstants.P_LOOP_FACTOR, 
				"Loop factor:", 
				getFieldEditorParent()));
		addField(new IntegerFieldEditor(
				PreferenceConstants.P_SWITCH_FACTOR, 
				"Branch factor:", 
				getFieldEditorParent()));
		addField(new IntegerFieldEditor(
				PreferenceConstants.P_METHOD_CALL_WEIGHT, 
				"Method invocation weight:", 
				getFieldEditorParent()));
		addField(new IntegerFieldEditor(
				PreferenceConstants.P_RECURSIVE_CALL_WEIGHT, 
				"Recursion weight:", 
				getFieldEditorParent()));
		addField(new IntegerFieldEditor(
				PreferenceConstants.P_LIBRARY_CALL_WEIGHT, 
				"Call to a method m from an external library (L) weight:", 
				getFieldEditorParent()));
		addField(new IntegerFieldEditor(
				PreferenceConstants.P_TRY_FACTOR, 
				"Try-[finally] (Tf) and  catch *[catch] (Cc)  weight:", 
				getFieldEditorParent()));
		addField(new RadioGroupFieldEditor(
				PreferenceConstants.P_STRATEGY, 
				"Call to an abstract method A, for which A0,..,An override A (Wax):",
				4,
				new String[][] {new String[]{"AVG", AverageWeightStrategy.class.getName()},
							   new String[]{"MIN", MinWeightStrategy.class.getName()},
							   new String[]{"MAX", MaxWeightStrategy.class.getName()},
							   new String[]{"SUM", SumWeightStrategy.class.getName()}
							  },
				 getFieldEditorParent())); 
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}
	
}