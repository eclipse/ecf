package org.eclipse.ecf.tutorial.scribbleshare.toolbox;

import java.io.Serializable;

import org.eclipse.swt.graphics.RGB;

public class DrawSettings implements Serializable {
	private int penWidth;

	private RGB backgroundColor;

	private RGB forgroundColor;
	
	private boolean isAntialaised;

	public DrawSettings() {
		penWidth = 1;
		backgroundColor = new RGB(255, 255, 255);
		forgroundColor = new RGB(0, 0, 0);
	}

	public RGB getBackgroundColor() {
		return backgroundColor;
	}

	public void setBackgroundColor(RGB backgroundColor) {
		this.backgroundColor = backgroundColor;
	}

	public RGB getForgroundColor() {
		return forgroundColor;
	}

	public void setForgroundColor(RGB forgroundColor) {
		this.forgroundColor = forgroundColor;
	}

	public int getPenWidth() {
		return penWidth;
	}

	public void setPenWidth(int penWidth) {
		this.penWidth = penWidth;
	}

	public boolean isAntialias() {
		return isAntialaised;
	}

	public void setAntialias(boolean isAntialaised) {
		this.isAntialaised = isAntialaised;
	}
}
