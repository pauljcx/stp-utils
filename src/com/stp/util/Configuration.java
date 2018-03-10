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
import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Set;
import java.util.Iterator;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.Cipher;

/** @author Paul Collins
 *  @version v1.01 ~ 10/25/2008
 *  HISTORY: Version 1.01 changed to static methods to allow static access to save settings from all classes in an application ~ 10/25/2008
 *			 Version 1.0 created Configuration class to support saving and loading user settings as key value pairs ~ 04/22/2008
 */
public class Configuration {
	private static final Logger logger = Logger.getLogger(Configuration.class.getName());
	public static final long serialVersionUID = 1L;
	
	private static Configuration instance = null;
	
	public static class DataStore implements XMLObject {
		public HashMap<String, Object> elements = new HashMap<String, Object>();
		public DataStore() {
		}
		public String[] getPropertyNames()	{
			return elements.keySet().toArray(new String[elements.size()]);
		}
		public Object getProperty(String name) {
			return elements.get(name);
		}
		public void setProperty(String name, Object value, String className) {
			if (value != null) {
				elements.put(name, value);
			}
		}
	}
	
	private final SecretKeySpec sks;
	private DataStore data;
	private File savefile = null;
	
	private Configuration() {
		sks = new SecretKeySpec(System.getProperty("user.name").concat(new String(new char[] {'7','$','5','!','2','#','0','b'})).getBytes(), 0, 8, "DES");
		data = new DataStore();
	}
	public static Configuration getInstance() {
		if (instance == null) {
			instance = new Configuration();
		}
		return instance;
	}
	public void setSaveFile(File file) {
		savefile = file;
	}
	public void save() {
		try {
			saveConfiguration();
		} catch (Exception ex) {
			logger.log(Level.WARNING, ex.getMessage());
		}
	}
	public void saveConfiguration(File file) {
		savefile = file;
		try {
			saveConfiguration();
		} catch (Exception ex) {
			logger.log(Level.WARNING, ex.getMessage());
		}
	}
	public void saveConfiguration() throws IOException {
		if (savefile != null) {
			JavaIO.saveXML(savefile, new DataStore[] { data });
		} else {
			throw new IOException("Save location not set.");
		}
	}
	public void loadConfiguration(File file) throws Exception {
		savefile = file;
		if (savefile.exists()) {
			DataStore loadedValues = (DataStore)XMLFileUtility.readXMLObject(new FileInputStream(savefile));
			if (loadedValues != null) {
				this.data = loadedValues;
			}
		}
	}
	public int getSize() {
		return data.elements.size();
	}
	public Set<String> getAllKeys() {
		return data.elements.keySet();
	}
	public String getSValue(String key) {
		return "" + getValue(key);
	}
	public String[] getSValues(String[] keys) {
		String[] values = new String[keys.length];
		for (int k = 0; k < keys.length; k++) {
			values[k] = getSValue(keys[k]);
		}
		return values;
	}
	public Object getValue(String key) {
		return data.elements.get(key);
	}
	public Object getValue(String key, Object defaultValue) {
		Object value = getValue(key);
		return (value != null) ? value : defaultValue;
	}
	public String getDecryptedValue(String key) {
		try {
			Object value = getValue(key);
			if (value != null) {
				return decrypt(value.toString(), sks);
			} else {
				return "";
			}
		} catch (Exception ex) {
			logger.log(Level.WARNING, "Decryption Failure: [" + key + "] " + ex.getMessage());
			return "";
		}
	}
	public String getString(String key) {
		return getString(key, "");
	}
	public String getString(String key, String defaultValue) {
		Object value = getValue(key);
		return (value != null) ? value.toString() : defaultValue;
	}
	public boolean getBool(String key) {
		return getBool(key, false);
	}
	public boolean getBool(String key, boolean defaultValue) {
		Object value = getValue(key);
		return (value instanceof Boolean) ? (Boolean)value : defaultValue;
	}
	public byte getByte(String key) {
		return getByte(key, (byte)0);
	}
	public byte getByte(String key, byte defaultValue) {
		Object value = getValue(key);
		return (value instanceof Byte) ? (Byte)value : defaultValue;
	}
	public short getShort(String key) {
		return getShort(key, (short)0);
	}
	public short getShort(String key, short defaultValue) {
		Object value = getValue(key);
		return (value instanceof Short) ? (Short)value : defaultValue;
	}
	public int getInt(String key) {
		return getInt(key, 0);
	}
	public int getInt(String key, int defaultValue) {
		Object value = getValue(key);
		return (value instanceof Integer) ? (Integer)value : defaultValue;
	}
	public long getLong(String key) {
		return getLong(key, 0L);
	}
	public long getLong(String key, long defaultValue) {
		Object value = getValue(key);
		return (value instanceof Long) ? (Long)value : defaultValue;
	}
	// Only sets the value if it doesn't already exist
	public void setDefault(String key, Object value) {
		Object result = getValue(key);
		if (result == null) {
			setValue(key, value);
		}
	}
	public void setValue(String key, Object value) {
		data.elements.put(key, value);
	}
	public void setEncryptedValue(String key, String value) {
		try {
			setValue(key, encrypt(value, sks));
		} catch (Exception ex) {
			logger.log(Level.WARNING, "Encryption Failure: [" + key + "] " + ex.getMessage());
		}
	}
	public boolean remove(String key) {
		return data.elements.remove(key) != null;
	}
	private String encrypt(String str, SecretKeySpec keySpec) throws Exception {
		// Encode the string into bytes using utf-8
		byte[] utf8 = str.getBytes("UTF8");

		// Encrypt
		Cipher cipher = Cipher.getInstance("DES");
		cipher.init(Cipher.ENCRYPT_MODE, keySpec);
		byte[] encoded = cipher.doFinal(utf8);
		
		// Encode bytes to hex string
		StringBuilder sb = new StringBuilder();
		for (byte b : encoded) {
			sb.append(String.format("%02X", b));
		}
		return sb.toString().trim();
	}
	private String decrypt(String str, SecretKeySpec keySpec) throws Exception {
		// Decode hex string to get bytes
		int len = str.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(str.charAt(i), 16) << 4) + Character.digit(str.charAt(i+1), 16));
		}
		// Decryp
		Cipher cipher = Cipher.getInstance("DES");
		cipher.init(Cipher.DECRYPT_MODE, keySpec);
		byte[] utf8 = cipher.doFinal(data);

		// Decode using utf-8
		return new String(utf8, "UTF8");
	}
}
