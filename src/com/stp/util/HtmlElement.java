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

/** @author Paul Collins
 *  @version v1.0 ~ 03/10/2018
 *  HISTORY:  Version 1.0 created HtmlElement class to store data retrieved from elements on a webpage ~ 03/10/2018
 */
public class HtmlElement {
	protected HtmlElement parent = null;
	protected String tagName = "";
	protected String id = "";
	protected String type = "";
	protected String cls = "";
	protected String name = "";
	protected String value = "";
	protected String src = "";
	protected String content = "";
	
	public HtmlElement() {
	}
	public HtmlElement(String tagText) {
		String[] parts = tagText.split("\\s+");
		setTagName(parts[0]);
		for (int a = 1; a < parts.length; a++) {
			int start = parts[a].indexOf("id=");
			if (start >= 0) {
				setId(parts[a].substring(start + 3).replace("\"", ""));
				continue;
			}
			start = parts[a].indexOf("name=");
			if (start >= 0) {
				setName(parts[a].substring(start + 5).replace("\"", ""));
				continue;
			}
		}
	}
	public HtmlElement getParent() {
		return parent;
	}
	public String getTagName() {
		return tagName;
	}
	public String getClosingTag() {
		return "</" + tagName + ">";
	}
	public String getId() {
		return id;
	}
	public String getType() {
		return type;
	}
	public String getElementClass() {
		return cls;
	}
	public String getName() {
		return name;
	}
	public String getValue() {
		return value;
	}
	public String getSource() {
		return src;
	}
	public String getContent() {
		return content;
	}
	public void setParent(HtmlElement parent) {
		this.parent = parent;
	}
	public void setTagName(String tagName) {
		this.tagName = tagName;
	}
	public void setId(String id) {
		this.id = id;
	}
	public void setType(String type) {
		this.type = type;
	}
	public void setElementClass(String cls) {
		this.cls = cls;
	}
	public void setName(String name) {
		this.name = name;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public void setSource(String src) {
		this.src = src;
	}
	public void setContent(String content) {
		this.content = content;
	}
	private String getIdString() {
		return (id.length() > 0) ? " id=" + getId() : "";
	}
	private String getTypeString() {
		return (type.length() > 0) ? " type=" + getType() : "";
	}
	private String getClassString() {
		return (cls.length() > 0) ? " class=" + getElementClass() : "";
	}
	private String getNameString() {
		return (name.length() > 0) ? " name=" + getName() : "";
	}
	private String getValueString() {
		return (value.length() > 0) ? " value=" + getValue() : "";
	}
	private String getSrcString() {
		return (src.length() > 0) ? " src=" + getSource() : "";
	}
	@Override
	public String toString() {
		return "<" + tagName + getIdString() + getTypeString() + getClassString() + getNameString() + getValueString() + getSrcString() + ">" + getContent() + getClosingTag();
	}
	@Override
	public boolean equals(Object other) {
		if (other instanceof HtmlElement) {
			if (id.length() > 0) {
				return id.equals(((HtmlElement)other).getId());
			} else {
				return tagName.equals(((HtmlElement)other).getTagName()) && content.equals(((HtmlElement)other).getContent());
			}
		}
		return false;
	}
}