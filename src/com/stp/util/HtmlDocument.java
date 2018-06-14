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
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.nio.charset.StandardCharsets;

/** @author Paul Collins
 *  @version v1.0 ~ 03/10/2018
 *  HISTORY:  Version 1.0 created a very simple light weight html parser to retreive data from websites ~ 03/10/2018
 */
public class HtmlDocument {
	public static class PostData {
		private String key = "";
		private String value = "";
		public PostData(String key, String value) {
			this.key = key;
			this.value = value;
		}
		public void setKey(String key) {
			this.key = key;
		}
		public void setValue(String value) {
			this.value = value;
		}
		@Override
		public String toString() {
			try {
				return URLEncoder.encode(key, "UTF-8") + "=" + URLEncoder.encode(value, "UTF-8");
			} catch (Exception ex) {
				return "";
			}
		}
	}
	protected final ArrayList<HtmlElement> elements = new ArrayList<HtmlElement>();
	protected URL website;
	protected HttpURLConnection resource;
	protected CookieManager cookieManager;
	
	public HtmlDocument() {
		cookieManager = new CookieManager();
		CookieHandler.setDefault(cookieManager);
	}
	public HtmlDocument(String urlString) {
		setURL(urlString);
		cookieManager = new CookieManager();
		CookieHandler.setDefault(cookieManager);
	}
	public void setURL(String urlString) {
		try {
			this.website = new URL(urlString);
		} catch (Exception ex) {
			// Log Exception
		}
	}
	public void destroySession() {
		cookieManager.getCookieStore().removeAll();
	}
	public int getResponseCode() {
		if (resource != null) {
			try {
				return resource.getResponseCode();
			} catch (Exception ex) {
				return -1;
			}
		} else {
			return -1;
		}
	}
	public boolean isHttpResponseOk() {
		return getResponseCode() == HttpURLConnection.HTTP_OK;
	}
	public void openConnection() throws Exception {
		if (website != null) {
			this.resource = (HttpURLConnection)website.openConnection();
		}
	}
	public void parseContent() throws Exception {
		if (website == null) {
			return;
		}
		elements.clear();
		BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()));
		String text = reader.readLine();
		HtmlElement lastParent = null;
		while (text != null) {
			lastParent = parse(text.trim(), lastParent);
			text = reader.readLine();
		}
		reader.close();
	}
	private HtmlElement parse(String html, HtmlElement lastParent) {
		int start = 0;
		int end = 0;
		start = html.indexOf("<");
		if (start >= 0) {
			end = html.indexOf(">");
			if (end >= 0) {
				String tagText = html.substring(start+1, end);
				// Check for closing tags
				if (tagText.indexOf("/") != 0) {
					HtmlElement element = new HtmlElement();
					element.setParent(lastParent);
					String[] parts = tagText.split("\\s+");
					element.setTagName(parts[0]);
					for (int a = 1; a < parts.length; a++) {
						start = parts[a].indexOf("id=");
						if (start >= 0) {
							element.setId(parts[a].substring(start + 3).replace("\"", ""));
							continue;
						}
						start = parts[a].indexOf("name=");
						if (start >= 0) {
							element.setName(parts[a].substring(start + 5).replace("\"", ""));
							continue;
						}
						start = parts[a].indexOf("value=");
						if (start >= 0) {
							element.setValue(parts[a].substring(start + 6).replace("\"", ""));
							continue;
						}
						start = parts[a].indexOf("src=");
						if (start >= 0) {
							element.setSource(parts[a].substring(start + 4).replace("\"", ""));
							continue;
						}
						start = parts[a].indexOf("type=");
						if (start >= 0) {
							element.setType(parts[a].substring(start + 5).replace("\"", ""));
							continue;
						}
						start = parts[a].indexOf("class=");
						if (start >= 0) {
							element.setElementClass(parts[a].substring(start + 6).replace("\"", ""));
							continue;
						}
					}
					elements.add(element);
					start = end + 1;
					boolean isVoidElement = (tagText.lastIndexOf("/") == tagText.length()-1);
					boolean hasClosingTag = isVoidElement;
					if (start < html.length()-1) {
						end = html.indexOf(element.getClosingTag());
						if (end < 0) {
							end = html.length();
						} else {
							hasClosingTag = true;
						}
						String content = html.substring(start, end);
						if (content.indexOf("<") >= 0) {
							parse(content, isVoidElement ? lastParent : element);
						} else {
							element.setContent(content);
						}
					}
					return (hasClosingTag) ? lastParent : element;
				} else {
					start = html.indexOf("<", end);
					HtmlElement previousParent = (lastParent != null) ? lastParent.getParent() : null;
					if (start >= 0) {
						parse(html.substring(start), previousParent);
					}
					return previousParent;
				}
			}
		}
		return lastParent;
	}
	public void sendPostData(PostData... data) throws Exception {
		if (website == null || data.length == 0) {
			return;
		}
		String message = data[0].toString();
		for (int i = 1; i < data.length; i++) {
			message = message + "&" + data[i].toString();
		}
		System.out.println("Post Message: " + message);
		byte[] outBytes = message.getBytes(StandardCharsets.UTF_8);
		
		resource = (HttpURLConnection)website.openConnection();
		resource.setRequestMethod("POST");
		resource.setDoOutput(true);
		resource.setFixedLengthStreamingMode(outBytes.length);
		resource.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
		resource.connect();
		
		OutputStream os = resource.getOutputStream();
		os.write(outBytes);
		os.close();
		parseContent();
	}
	public ArrayList<HtmlElement> getElements() {
		return elements;
	}
	public String[] getChildContent(HtmlElement parent) {
		ArrayList<String> contentList = new ArrayList<String>();
		for (HtmlElement e : elements) {
			if (e.getParent() != null && e.getParent().equals(parent)) {
				contentList.add(e.getContent());
			}
		}
		return contentList.toArray(new String[contentList.size()]);
	}
	public ArrayList<HtmlElement> getChildElements(HtmlElement parent) {
		ArrayList<HtmlElement> children = new ArrayList<HtmlElement>();
		for (HtmlElement e : elements) {
			if (e.getParent() != null && e.getParent().equals(parent)) {
				children.add(e);
			}
		}
		return children;
	}
	public String getContentById(String id) {
		for (HtmlElement e : elements) {
			if (e.getId().equals(id)) {
				return e.getContent();
			}
		}
		return "";
	}
	public HtmlElement getElementById(String id) {
		for (HtmlElement e : elements) {
			if (e.getId().equals(id)) {
				return e;
			}
		}
		return null;
	}
	public HtmlElement addElement(HtmlElement element) {
		elements.add(element);
		return element;
	}
	public void removeElement(HtmlElement element) {
		elements.remove(element);
	}
	public void clear() {
		elements.clear();
	}
}