package org.eclipse.ecf.tutorial.scribbleshare.toolbox;

import java.io.Serializable;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;

abstract public class AbstractTool implements Serializable {
	protected int startX, startY, endX, endY;
	
	protected boolean penDown = false;
	
	protected boolean isComplete = false;
	
	protected DrawSettings drawSettings;
/*	
	public void setCoordinates(int startX, int startY, int endX, int endY) {
		this.startX = startX;
		this.startY = startY;
		this.endX = endX;
		this.endY = endY;
	}	*/

	abstract public String getName();
	
	abstract public Image getImage();
	
	abstract public void draw(final Canvas canvas);	
	
	abstract public void handleUIEvent(Event event, Canvas canvas);
	
	protected void setupGC(GC gc) {
		gc.setLineWidth(drawSettings.getPenWidth());
		if (drawSettings.isAntialias()) {
			gc.setAntialias(SWT.ON);
		} else {
			gc.setAntialias(SWT.OFF);
		}

	}
	
	public boolean isPenDown() {
		return penDown;
	}
	
	public boolean isComplete() {
		return isComplete;
	}

	public void setComplete(boolean b) {
		isComplete = b;
	}

	public void setDrawSettings(DrawSettings drawSettings) {
		this.drawSettings = drawSettings;
	}
}
