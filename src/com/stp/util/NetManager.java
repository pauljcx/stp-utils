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
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

/** @author Paul Collins
 *  @version v1.0 ~ 03/10/2018
 *  HISTORY:  Version 1.0 created a utility class to retrieve data from websites ~ 03/10/2018
 */
public class NetManager {
	// Returns a single line of text from the specified website url
	public static final String getWebResponse(String urlString) throws Exception {
		URL website = new URL(urlString);
		URLConnection resource = website.openConnection();
		resource.setReadTimeout(5000);
		BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()));
		String response = reader.readLine();
		reader.close();
		return response;
	}
	// Returns all lines of text from the specifed website url
	public static final ArrayList<String> getWebData(String urlString) throws Exception {
		URL website = new URL(urlString);
		URLConnection resource = website.openConnection();
		BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()));
		ArrayList<String> results = new ArrayList<String>();
		String text = reader.readLine();
		while (text != null) {
			results.add(text);
			text = reader.readLine();
		}
		reader.close();
		return results;
	}
	// Reads all lines of text from the specifed website url and parses the html into it's elements
	public static final HtmlDocument getWebDocument(String urlString) throws Exception {
		HtmlDocument document = new HtmlDocument(urlString);
		document.openConnection();
		document.parseContent();
		return document;
	}
}