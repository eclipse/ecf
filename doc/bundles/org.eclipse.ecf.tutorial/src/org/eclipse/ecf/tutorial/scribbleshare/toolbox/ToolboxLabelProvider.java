package org.eclipse.ecf.tutorial.scribbleshare.toolbox;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;

public class ToolboxLabelProvider implements ITableLabelProvider {

	public Image getColumnImage(Object element, int columnIndex) {
		if (element instanceof AbstractTool) {
			return ((AbstractTool)element).getImage();
		}
		return null;
	}

	public String getColumnText(Object element, int columnIndex) {
		if (element instanceof AbstractTool) {
			return ((AbstractTool)element).getName();
		}
		return "[UNKNOWN TYPE IN LABEL PROVIDER: " + element.getClass().toString() + "]";
	}

	public void addListener(ILabelProviderListener listener) {
	}

	public void dispose() {
	}

	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	public void removeListener(ILabelProviderListener listener) {
	}

}
