package ar.edu.unicen.ccm.out;

import java.io.ByteArrayInputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

import ar.edu.unicen.ccm.utils.MapFun;
import ar.edu.unicen.ccm.utils.Utils;

public class CSVWriter {
	IFile file;
	StringBuilder builder;
	int fields;
	
		
	public CSVWriter(IProject project, String fileName, Object... headers) throws CoreException {
		IFile f = project.getProject().getFile(fileName);
		if (f.exists())
			f.delete(true, null);
		this.file = f;
		
		
		this.builder = new StringBuilder(formatRow(headers));
		builder.append("\r\n");
		this.fields = headers.length;
	}
	
	public void addRow(Object... row) throws Exception {
		if (row.length != fields)
			throw new Exception("Invalid CSV row");
		this.builder.append(formatRow(row)).append("\r\n");
		
	}
	
	public void save() throws CoreException {
		this.file.create(new ByteArrayInputStream(this.builder.toString().getBytes()), true, null);
	}
	
	private String formatRow(Object... row) {
		return Utils.join(quote(row), ",");
	}
	private Object[] quote(Object[] params) {
		return Utils.map(params, new MapFun() {
			public Object map(Object item) {
				return "\"" + item + "\"";
			}
		});
	}
	
	

}
