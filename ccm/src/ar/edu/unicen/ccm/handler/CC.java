package ar.edu.unicen.ccm.handler;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Vector;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import ar.edu.unicen.ccm.bcs.MethodNode;
import ar.edu.unicen.ccm.model.ClassComplexityInfo;
import ar.edu.unicen.ccm.model.CostModel;
import ar.edu.unicen.ccm.model.HierarchyComplexityInfo;
import ar.edu.unicen.ccm.out.CSVWriter;

/**
 * This is the hanlder invoked when the user activate the
 * menu in the package explorer.  
 * @author pablo
 *
 */
public class CC extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IStructuredSelection selection = (IStructuredSelection) HandlerUtil.getActiveMenuSelection(event);
		IJavaProject project = (IJavaProject)selection.getFirstElement();
		try {
			analyzeJavaProject(project);
		} catch (Exception e) {
			throw new ExecutionException("Error calculating metric", e);
		}

		return null;
	}

	private void analyzeJavaProject(final IJavaProject project) throws Exception {
		Job job = new Job("Calculating code complexity") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					//TODO: most of the time is spent on parsing all the java source files
					//      rather than on analyzing it.  So we I need a better way to show the progress
					//      maybe by first counting the files and report progression based on that.
					CostModel cm = new CostModel(project);
					int totalWork = cm.getTypes().size() +	 cm.getDependencyModel().getMethods().size() + cm.getDependencyModel().getRootClasses().size();
					monitor.beginTask("Calculating ..", totalWork);
					int i = 1;
					CSVWriter csv = new CSVWriter(project.getProject(),"mc.csv", "id", "method", "weight", "weightExpression", "externalCalls");
					for(IType t : cm.getTypes()) {
						ClassComplexityInfo info = cm.getClassComplexityInfo(t.getFullyQualifiedName('.'));
						for (MethodNode mn:  info.getMethods().values()) {
							csv.addRow(i++, mn.getSignature(), mn.getCost(), mn.getExpr(), mn.getExternalCalls());
							monitor.worked(1);
						}
					}
					
					csv.save();
					
					csv = new CSVWriter(project.getProject(), "wcc.csv", 
							"id", "className", "superClassName", "numberOfMethods", "ac",  "wcc", "cc");
					Collection<IType> superclasses = new Vector<IType>();
					i = 1;
					for(IType t : cm.getTypes()) {
						ClassComplexityInfo info = cm.getClassComplexityInfo(t.getFullyQualifiedName('.'));
						IType superClassType = cm.getDependencyModel().getSuperClass(t);
						String superclass = (superClassType != null) ? superClassType.getFullyQualifiedName('.') : "java.lang.Object";
						HierarchyComplexityInfo cost = cm.hierarchyCostOf(info.getName());						
						csv.addRow(i++,info.getName(), superclass, info.getMethods().size(), info.getAttrComplexity(), info.getWeightedClassComplexity(), cost.getCost());						
						if (!superclasses.contains(superClassType))
							superclasses.add(superClassType);
						monitor.worked(1);
					}
					for (IType iType : superclasses) {
						csv.addRow(i++, iType.getFullyQualifiedName('.'), "LEGACY", "10", "10", "1","1");
						monitor.worked(1);
					}
					csv.save();

					csv = new CSVWriter(project.getProject(), "code_complexity.csv", "id", "hierarchy", "cc", "numberOfClasses", "depth", "expression");
					i = 1;
					BigInteger totalCost = BigInteger.valueOf(0);
					int classes = 0;
					int max_depth = 0;
					for(String root : cm.getDependencyModel().getRootClasses()) {
						HierarchyComplexityInfo cost = cm.hierarchyCostOf(root);
						csv.addRow(i++, root, cost.getCost(), cost.getClasses(), cost.getDepth(), cost.getExpr());
						totalCost = totalCost.add(cost.getCost());
						max_depth = Math.max(max_depth, cost.getDepth());
						classes += cost.getClasses();
						monitor.worked(1);
					}
					csv.addRow(i++, "TOTAL", totalCost, classes, max_depth, "-");
					csv.save();

				} catch (Exception e ) {
					e.printStackTrace();
				} finally {
					monitor.done();
				}
				return Status.OK_STATUS;
			}

		};
		job.setUser(true);
		job.schedule();
	}



}
