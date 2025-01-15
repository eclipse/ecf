/*******************************************************************************
 * Copyright (c) 2020 Paul Verest, Benjamin Gurok, Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Apache Public License v2.0 which 
 * accompanies this distribution, and is available at 
 * https://www.apache.org/licenses/LICENSE-2.0
 * 
 * Contributors: Paul Verest, Benjamin Gurok, and Composent, Inc. - initial 
 * API and implementation
 ******************************************************************************/
package org.eclipse.ecf.remoteservices.tooling.bndtools.editors;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ecf.remoteservices.tooling.bndtools.Activator;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

/**
 * Converts RGB to Color, reuses the existing Color instances. A singleton.
 * @author Benjamin gurok
 * @author Paul Verest
 */
public class EditorColors {

    private static final Map<Integer, Color> intToColor = new HashMap<Integer, Color>();

    public static Color getColor(RGB rgb) {
        Integer colorInt = rgbToInteger(rgb);
        Color color = intToColor.get(colorInt);
        if (color == null) {
            color = new Color(Display.getDefault(), rgb);
            intToColor.put(colorInt, color);
        }
        return color;
    }
    
    public static Color getColor(String preference) {
    	return getColor(PreferenceConverter.getColor(Activator.getDefault().getPreferenceStore(), preference));
    }

    private static Integer rgbToInteger(RGB rgb) {
        return ((rgb.red & 0xFF) << 16) + ((rgb.green & 0xFF) << 8) + (rgb.blue & 0xFF);
    }
}
