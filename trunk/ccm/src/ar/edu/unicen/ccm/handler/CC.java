package ar.edu.unicen.ccm.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import ar.edu.unicen.ccm.bcs.MethodNode;
import ar.edu.unicen.ccm.bcs.MethodSignature;
import ar.edu.unicen.ccm.model.ClassComplexityInfo;
import ar.edu.unicen.ccm.model.CostModel;
import ar.edu.unicen.ccm.out.CSVWriter;

/**
 * This is the hanlder invoked when the user activate the
 * menu un the package explorer.  
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
	
	private void analyzeJavaProject(IJavaProject project) throws Exception {
		 CostModel cm = new CostModel(project);
		 
		 CSVWriter csv = new CSVWriter(project.getProject(),"mc.csv", "Method", "Weight", "Weight Expression");
		 for (MethodDeclaration m : cm.getDependencyModel().getMethods()) {
			 MethodNode mn = cm.getMethodComplexity(MethodSignature.from(m.resolveBinding()));
			 csv.addRow(mn.getSignature(), mn.getCost(), mn.getExpr());
		 }
		 csv.save();
		 
		 csv = new CSVWriter(project.getProject(), "wcc.csv", 
					"Class", "# methods", "# attributes",  "Weight");
		 for(TypeDeclaration t : cm.getTypes()) {
			 ITypeBinding tb = t.resolveBinding();
			 ClassComplexityInfo info = cm.getClassComplexityInfo(tb.getQualifiedName());
			 csv.addRow(info.getName(), info.getMethods().size(), info.getAttrComplexity(), info.getWeightedClassComplexity());
		 }
		 csv.save();
		 
		 csv = new CSVWriter(project.getProject(), "code_complexity.csv", "Hierarchy", "Weight");
		 int totalCost = 0;
		 for(String root : cm.getDependencyModel().getRootClasses()) {
			 int cost = cm.hierarchyCostOf(root);
			 csv.addRow(root, cost);
			 totalCost += cost;
		 }
		 csv.addRow("TOTAL", totalCost);
		 csv.save();
	}
	


}
