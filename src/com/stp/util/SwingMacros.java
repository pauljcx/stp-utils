/* MIT License
 *
 * Copyright (c) 2018 Paul Collins
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.stp.util;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.ImageIcon;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.Image;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.image.BufferedImage;
import java.awt.event.ActionListener;
import java.util.HashMap;
import javax.imageio.ImageIO;

/** The SwingMacros class provides convienence methods to setup common GUI elements */
public final class SwingMacros
{
	/* Stores the currently set Swing background color used for opaque components */
	public static final Color BACKGROUND = new Color(new JPanel().getBackground().getRGB());
	
	/* A blank image used when an image resource isn't located correctly */
	public static final BufferedImage BLANK_IMAGE = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
	
	/* A map containing icon images that have already been loaded previously to allow resource sharing */
	private static HashMap<String, BufferedImage> IconImages = new HashMap<String, BufferedImage>();
	private static HashMap<String, Color> DefaultColors = new HashMap<String, Color>();
	
	public static ImageIcon getIcon(String path) {
		return new ImageIcon(getIconImage(path));
	}
	public static Image getIconImage(String path) {
		if (IconImages.containsKey(path)) {
			return IconImages.get(path);
		} else {
			BufferedImage image = BLANK_IMAGE;
			try {
				image = ImageIO.read(JavaIO.getInputStream(path));
				if (image != null) {
					IconImages.put(path, image);
				} else {
					image = BLANK_IMAGE;
				}
			} catch (Exception ex) { 
				System.err.println("Error Retrieving Image: " + path);
			}
			return image;
		}
	}
	public static void registerDefaultColor(String key, Color color) {
		DefaultColors.put(key, color);
	}
	public static Color getDefaultColor(String key) {
		Color result = DefaultColors.get(key);
		if (result != null) {
			return result;
		} else {
			return BACKGROUND;
		}
	}
	public static JButton createButton(String text, String command, ActionListener listener) {
		JButton button = new JButton(text);
		button.setActionCommand(command);
		button.addActionListener(listener);
		return button;
	}
	public static JButton createButton(String text, String command, int mnemonic, ActionListener listener) {
		JButton button = new JButton(text);
		button.setActionCommand(command);
		button.setMnemonic(mnemonic);
		button.addActionListener(listener);
		return button;
	}
	public static JButton createButton(String iconPath, String command, String tooltip, int mnemonic, ActionListener listener) {
		return createButton(getIcon(iconPath), command, tooltip, mnemonic, listener);
	}
	public static JButton createButton(Icon icon, String command, String tooltip, int mnemonic, ActionListener listener) {
		JButton button = new JButton("", icon);
		button.setActionCommand(command);
		button.setMnemonic(mnemonic);
		button.setToolTipText(tooltip);
		button.addActionListener(listener);
		button.setPreferredSize(new Dimension(24, 24));
		return button;
	}
	public static JButton createFlatButton(String iconPath, String command, String tooltip, int mnemonic, ActionListener listener) {
		return createFlatButton(getIcon(iconPath), command, tooltip, mnemonic, listener);
	}
	public static JButton createFlatButton(Icon icon, String command, String tooltip, int mnemonic, ActionListener listener) {
		JButton button = new JButton("", icon);
		button.setActionCommand(command);
		button.setMnemonic(mnemonic);
		button.setToolTipText(tooltip);
		button.addActionListener(listener);
		button.setPreferredSize(new Dimension(icon.getIconWidth()+8, icon.getIconHeight()+8));
		button.setBackground(getDefaultColor("button"));
		return button;
	}
	public static JButton createButton(String text, String iconPath, String command, String tooltip, int mnemonic, ActionListener listener) {
		return createButton(text, getIcon(iconPath), command, tooltip, mnemonic, listener);
	}
	public static JButton createButton(String text, Icon icon, String command, String tooltip, int mnemonic, ActionListener listener) {
		JButton button = new JButton(text, icon);
		button.setActionCommand(command);
		button.setMnemonic(mnemonic);
		button.setToolTipText(tooltip);
		button.addActionListener(listener);
		return button;
	}
	public static JMenuItem createMenuItem(String text, String command, ActionListener listener) {
		JMenuItem item = new JMenuItem(text);
		item.setActionCommand(command);
		item.addActionListener(listener);
		return item;
	}
	public static JMenuItem createMenuItem(String text, String iconPath, String command, ActionListener listener) {
		return createMenuItem(text, getIcon(iconPath), command, listener);
	}
	public static JMenuItem createMenuItem(String text, Icon icon, String command, ActionListener listener) {
		JMenuItem item = new JMenuItem(text, icon);
		item.setActionCommand(command);
		item.addActionListener(listener);
		return item;
	}
	public static Color getDefaultBackground() {
		return BACKGROUND;
	}
	public static JPanel getTitledComponent(String title, Component component) {
		component.setName(title);
		JPanel panel = new JPanel(new GridLayout(1, 1));
		panel.add(component);
		panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), title));
		return panel;
	}
	public static JPanel getNamedComponent(String name, Component component) {
		component.setName(name);
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(new JLabel(name), BorderLayout.WEST);
		panel.add(component, BorderLayout.CENTER);
		return panel;
	}
}