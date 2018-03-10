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
import java.awt.Color;
import java.io.*;
import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.lang.reflect.Constructor;
import java.lang.reflect.Array;

/* @author Paul Collins
 * @version v1.0 ~ 03/10/2018
 * HISTORY: Version 1.0 created utility class for saving and loading objects as xml text files
 */
public final class XMLFileUtility {
	private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(XMLFileUtility.class.getName());
	private static final Map<String, Class<?>> typeMap = new HashMap<String, Class<?>>();
	private static final Map<Class<?>, Class<?>> primMap = new HashMap<Class<?>, Class<?>>();
	private static final Map<Integer, String> charMap = new HashMap<Integer, String>();
    static {
		try {
			typeMap.put("Byte", Class.forName("java.lang.Byte"));
			typeMap.put("Short", Class.forName("java.lang.Short"));
			typeMap.put("Integer", Class.forName("java.lang.Integer"));
			typeMap.put("Long", Class.forName("java.lang.Long"));
	        typeMap.put("Float", Class.forName("java.lang.Float"));
	        typeMap.put("Double", Class.forName("java.lang.Double"));
			typeMap.put("Boolean", Class.forName("java.lang.Boolean"));
			typeMap.put("Char", Class.forName("java.lang.Character"));
			typeMap.put("String", Class.forName("java.lang.String"));
			typeMap.put("Date", Class.forName("java.util.Date"));
			typeMap.put("Color", Class.forName("java.awt.Color"));
			typeMap.put("HashMap", Class.forName("java.util.HashMap"));
			typeMap.put("FloatBuffer", Class.forName("java.nio.FloatBuffer"));
			typeMap.put("IntBuffer", Class.forName("java.nio.IntBuffer"));
			primMap.put(Class.forName("java.lang.Byte"), Byte.TYPE);
			primMap.put(Class.forName("java.lang.Short"), Short.TYPE);
			primMap.put(Class.forName("java.lang.Integer"), Integer.TYPE);
			primMap.put(Class.forName("java.lang.Long"), Long.TYPE);
	        primMap.put(Class.forName("java.lang.Float"), Float.TYPE);
			primMap.put(Class.forName("java.lang.Double"), Double.TYPE);
			primMap.put(Class.forName("java.lang.Boolean"), Boolean.TYPE);
			primMap.put(Class.forName("java.lang.Character"), Character.TYPE);
			primMap.put(Class.forName("java.nio.HeapIntBuffer"), Class.forName("java.nio.IntBuffer"));
			primMap.put(Class.forName("java.nio.HeapFloatBuffer"), Class.forName("java.nio.FloatBuffer"));
			charMap.put(34, "&quot;");
			charMap.put(38, "&amp;");
			charMap.put(60, "&lt;");
			charMap.put(62, "&gt;");
			charMap.put(160, "&nbsp;");
			charMap.put(161, "&iexcl;");
			charMap.put(162, "&cent;");
			charMap.put(163, "&pound;");
			charMap.put(164, "&curren;");
			charMap.put(165, "&yen;");
			charMap.put(166, "&brvbar;");
			charMap.put(167, "&sect;");
			charMap.put(168, "&uml;");
			charMap.put(169, "&copy;");
			charMap.put(170, "&ordf;");
			charMap.put(171, "&laquo;");
			charMap.put(172, "&not;");
			charMap.put(173, "&shy;");
			charMap.put(174, "&reg;");
			charMap.put(175, "&macr;");
			charMap.put(176, "&deg;");
			charMap.put(177, "&plusmn;");
			charMap.put(178, "&sup2;");
			charMap.put(179, "&sup3;");
			charMap.put(180, "&acute;");
			charMap.put(181, "&micro;");
			charMap.put(182, "&para;");
			charMap.put(183, "&middot;");
			charMap.put(184, "&cedil;");
			charMap.put(185, "&sup1;");
			charMap.put(186, "&ordm;");
			charMap.put(187, "&raquo;");
			charMap.put(188, "&frac14;");
			charMap.put(189, "&frac12;");
			charMap.put(190, "&frac34;");
			charMap.put(191, "&iquest;");
			charMap.put(192, "&Agrave;");
			charMap.put(193, "&Aacute;");
			charMap.put(194, "&Acirc;");
			charMap.put(195, "&Atilde;");
			charMap.put(196, "&Auml;");
			charMap.put(197, "&Aring;");
			charMap.put(198, "&AElig;");
			charMap.put(199, "&Ccedil;");
			charMap.put(200, "&Egrave;");
			charMap.put(201, "&Eacute;");
			charMap.put(202, "&Ecirc;");
			charMap.put(203, "&Euml;");
			charMap.put(204, "&Igrave;");
			charMap.put(205, "&Iacute;");
			charMap.put(206, "&Icirc;");
			charMap.put(207, "&Iuml;");
			charMap.put(208, "&ETH;");
			charMap.put(209, "&Ntilde;");
			charMap.put(210, "&Ograve;");
			charMap.put(211, "&Oacute;");
			charMap.put(212, "&Ocirc;");
			charMap.put(213, "&Otilde;");
			charMap.put(214, "&Ouml;");
			charMap.put(215, "&times;");
			charMap.put(216, "&Oslash;");
			charMap.put(217, "&Ugrave;");
			charMap.put(218, "&Uacute;");
			charMap.put(219, "&Ucirc;");
			charMap.put(220, "&Uuml;");
			charMap.put(221, "&Yacute;");
			charMap.put(222, "&THORN;");
			charMap.put(223, "&szlig;");
			charMap.put(224, "&agrave;");
			charMap.put(225, "&aacute;");
			charMap.put(226, "&acirc;");
			charMap.put(227, "&atilde;");
			charMap.put(228, "&auml;");
			charMap.put(229, "&aring;");
			charMap.put(230, "&aelig;");
			charMap.put(231, "&ccedil;");
			charMap.put(232, "&egrave;");
			charMap.put(233, "&eacute;");
			charMap.put(234, "&ecirc;");
			charMap.put(235, "&euml;");
			charMap.put(236, "&igrave;");
			charMap.put(237, "&iacute;");
			charMap.put(238, "&icirc;");
			charMap.put(239, "&iuml;");
			charMap.put(240, "&eth;");
			charMap.put(241, "&ntilde;");
			charMap.put(242, "&ograve;");
			charMap.put(243, "&oacute;");
			charMap.put(244, "&ocirc;");
			charMap.put(245, "&otilde;");
			charMap.put(246, "&ouml;");
			charMap.put(247, "&divide;");
			charMap.put(248, "&oslash;");
			charMap.put(249, "&ugrave;");
			charMap.put(250, "&uacute;");
			charMap.put(251, "&ucirc;");
			charMap.put(252, "&uuml;");
			charMap.put(253, "&yacute;");
			charMap.put(254, "&thorn;");
			charMap.put(255, "&yuml;");
		} catch (Exception e) { e.printStackTrace(); }
	}
	public static void registerObject(String className, Class cls) {
		typeMap.put(className, cls);
	}
	public static Object readXMLObject(InputStream stream) {
		try {
			Object array = readXMLObjects(stream);
			return Array.get(array, 0);
		} catch (Exception ex) {
			return null;
		}
	}
	public static Object readXMLObjects(InputStream stream) throws Exception {
		DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document doc = parser.parse(stream);
		NodeList rootList = doc.getElementsByTagName("SavedObjects");
		ArrayList<Object> nodes = new ArrayList<Object>();
		Class<?> objClass = null;
		for (int r = 0; r < rootList.getLength(); r++) {
			objClass = Class.forName(rootList.item(r).getAttributes().getNamedItem("class").getNodeValue());
			NodeList childList = rootList.item(r).getChildNodes();
			for (int c = 0; c < childList.getLength(); c++) {
				if (childList.item(c).getNodeType() == Node.ELEMENT_NODE) {
					Object result = getObject(childList.item(c), objClass);
					if (result != null) {
						nodes.add(result);
					}
				}
			}
		}
		Object xmlObjects = Array.newInstance(objClass, nodes.size());
		for (int s = 0; s < nodes.size(); s++) {
			Array.set(xmlObjects, s, nodes.get(s));
		}
		return xmlObjects;
	}
	private static Object getObject(Node child, Class<?> childClass) throws Exception {
		Constructor<?> constructor = null;
		Class<?> valClass;
		NodeList valueList = child.getChildNodes();
		if (XMLObject.class.isAssignableFrom(childClass)) {
			XMLObject object = (XMLObject)childClass.newInstance();
			for (int v = 0; v < valueList.getLength(); v++) {
				if (valueList.item(v).getNodeType() == Node.ELEMENT_NODE) {
					Node nodeClass = valueList.item(v).getAttributes().getNamedItem("class");
					Node nodeName = valueList.item(v).getAttributes().getNamedItem("name");
					valClass = (nodeClass != null) ? typeMap.get(nodeClass.getNodeValue()) : null;
					if (valClass != null) {
						try {
							object.setProperty(nodeName.getNodeValue(), valClass.cast(formatAs(valueList.item(v).getTextContent(), valClass)), valClass.getSimpleName());
						} catch (Exception ex) {
							logger.log(Level.WARNING, childClass.getSimpleName() + " | Node: " + nodeName + " | Failure to cast object as: " + valClass);
						}
					} else {
						object.setProperty(nodeName.getNodeValue(), valueList.item(v).getTextContent(), nodeClass.getNodeValue());
					}
				}
			}
			return object;
		} else {
			try { 
				return childClass.getConstructor(new Class<?>[] { String.class }).newInstance(new Object[] { child.getTextContent() });
			} catch (Exception ex) {
				logger.log(Level.WARNING, "Failure to create object: " + childClass + ", " + child.getTextContent());
				return null;
			}
		}
	}
	public static void saveXMLPrimatives(OutputStream stream, Object[] objects) throws Exception {
		if (objects == null || objects.length == 0) {
			throw new Exception("Invalid object array.");
		} else {
			String DTDInfo = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
			String openTag = "<SavedObjects class=\"" + objects[0].getClass().getName() + "\">\n";
			String closeTag = "</SavedObjects>";
			String childInfo;
			stream.write(DTDInfo.getBytes(), 0, DTDInfo.length());
			stream.write(openTag.getBytes(), 0, openTag.length());
			for (int i = 0; i < objects.length; i++) {
				childInfo = "\t<object class=\"" + objects[i].getClass().getSimpleName() + "\">" + objects[i].toString() + "</object>\n";
				stream.write(childInfo.getBytes(), 0, childInfo.length());
			}
			stream.write(closeTag.getBytes(), 0, closeTag.length());
			stream.close();
		}
	}
	public static void saveXMLObject(OutputStream stream, XMLObject obj) throws Exception {
		saveXMLObjects(stream, new XMLObject[] { obj });
	}
	public static void saveXMLObjects(OutputStream stream, XMLObject[] xmlObject) throws Exception {
		if (xmlObject == null || xmlObject.length == 0) {
			throw new Exception("Invalid object array.");
		} else {
			String DTDInfo = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
			String openTag = "<SavedObjects class=\"" + xmlObject[0].getClass().getName() + "\">\n";
			String closeTag = "</SavedObjects>";
			String child;
			String childInfo;
			String childType;
			Object[] childNodes;
			String[] nodeNames;
			stream.write(DTDInfo.getBytes(), 0, DTDInfo.length());
			stream.write(openTag.getBytes(), 0, openTag.length());
			for (int i = 0; i < xmlObject.length; i++) {
				if (xmlObject[i] == null) {
					logger.log(Level.INFO, "Skipped writing null object.");
					continue;
				}
				child = xmlObject[i].getClass().getSimpleName();
				childInfo = "\t<object class=\"" + child + "\">\n";
				for (String nodeName : xmlObject[i].getPropertyNames()) {
					Object nodeValue = xmlObject[i].getProperty(nodeName);
					if (nodeValue != null) {
						childInfo = childInfo + "\t\t<param name=\"" + nodeName + "\" class=\"" + nodeValue.getClass().getSimpleName() + "\">" + encode(nodeValue) + "</param>\n";
					} else {
						logger.log(Level.INFO, "Skipped writing null parameter: " + nodeName);
					}
				}
				childInfo = childInfo + "\t</object>\n";
				stream.write(childInfo.getBytes(), 0, childInfo.length());
			}
			stream.write(closeTag.getBytes(), 0, closeTag.length());
			stream.close();
		}
	}
	private static String encode(Object obj) {
		String input = obj.toString();
		String result = "";
		for (int c = 0; c < input.length(); c++) {
			char ch = input.charAt(c);
			String encoded = charMap.get((int)ch);
			if (encoded != null) {
				result = result + encoded;
			} else {
				result = result + ch;
			}
		}
		return result;
	}
	public static Class<?>[] getObjectClasses(Object[] objects) {
		Class<?>[] classes = new Class<?>[objects.length];
		Class primClass;
		for (int i = 0; i < objects.length; i++) {
			//System.out.println(i + " Object: " + objects[i]);
			primClass = primMap.get(objects[i].getClass());
			if (primClass != null) {
				classes[i] = primClass;
			} else {
				classes[i] = objects[i].getClass();
			}
		}
		return classes;
	}
	public static Object formatAs(Object obj, Class cls) throws Exception {
		if (cls.isInstance(obj)) {
			return obj;
		} else if (cls.equals(String.class)) {
			return (obj != null) ? obj.toString() : "";
		} else if (cls.equals(Double.TYPE) || cls.equals(Double.class)) {
			return (obj != null) ? Double.valueOf(obj.toString()) : 0.0;
		} else if (cls.equals(Float.TYPE) || cls.equals(Float.class)) {
			return (obj != null) ? Float.valueOf(obj.toString()) : 0.0f;
		} else if (cls.equals(Long.TYPE) || cls.equals(Long.class)) {
			return (obj != null) ? Long.valueOf(obj.toString()) : 0L;
		} else if (cls.equals(Integer.TYPE) || cls.equals(Integer.class)) {
			return (obj != null) ? Integer.valueOf(obj.toString()) : 0;
		} else if (cls.equals(Short.TYPE) || cls.equals(Short.class)) {
			return (obj != null) ? Short.valueOf(obj.toString()) : (short)0;
		} else if (cls.equals(Byte.TYPE) || cls.equals(Byte.class)) {
			return (obj != null) ? Byte.valueOf(obj.toString()) : (byte)0;
		} else if (cls.equals(Boolean.TYPE) || cls.equals(Boolean.class)) {
			return (obj != null) ? new Boolean(obj.toString()) : false;
		} else if (cls.equals(Class.forName("java.sql.Date"))) {
			return java.sql.Date.valueOf(obj.toString());
		} else if (cls.equals(Class.forName("java.util.Date"))) {
			return CustomDateFormat.getDate(obj.toString());
		} else if (cls.equals(Class.forName("java.awt.Color"))) {
			String val = obj.toString();
			int r = 0;
			int g = 0;
			int b = 0;
			int s = val.indexOf("r=");
			if(s > 0) {
				int e = val.indexOf(",", s);
				r = Integer.valueOf(val.substring(s + 2, e));
				s = val.indexOf("g=");
				e = val.indexOf(",", s);
				g = Integer.valueOf(val.substring(s + 2, e));
				s = val.indexOf("b=");
				e = val.indexOf("]", s);
				b = Integer.valueOf(val.substring(s + 2, e));
			}
			return new Color(r, g, b);
		} else if (cls.equals(Class.forName("java.util.HashMap"))) {
			String[] entries = obj.toString().replace("{", "").replace("}", "").split(",");
			for (String e : entries) {
				String[] parts = e.trim().split("=");
			}
			return null;
		} else {
			return null;
		}
	}
}