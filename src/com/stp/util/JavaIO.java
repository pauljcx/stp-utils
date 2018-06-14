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
import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.net.URL;
import java.util.HashMap;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Set;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Array;

/** The JavaIO class provides easy access to file system operations through static methods */
public class JavaIO {
	private static final Class[] primatives = { Boolean.class, Character.class, Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class, String.class };
	
	public static InputStream getInputStream(String path) {
		return getInputStream(JavaIO.class, path);
	}
	public static InputStream getInputStream(Class cls, String path) {
		InputStream input = cls.getResourceAsStream(path);
		if (input == null) {
			try {
				input = new FileInputStream(path);
				return input;
			} catch (Exception ex) {
				return null;
			}
		}
		return input;
	}
	/** Copies the contents of on file to another creating a new destination file if one doesn't already exist at the path specified.
	*/
	public static void copyFile(File sourceFile, File destFile) throws IOException {
		if(!destFile.exists()) {
			destFile.createNewFile();
		}
		FileChannel source = null;
		FileChannel destination = null;

		try {
			source = new FileInputStream(sourceFile).getChannel();
			destination = new FileOutputStream(destFile).getChannel();
			destination.transferFrom(source, 0, source.size());
		} finally {
			if (source != null) {
				source.close();
			}
			if (destination != null) {
				destination.close();
			}
		}
	}
	public static void copyFile(Class cls, String srcFile, File destFile) throws IOException {
		copyFile(getInputStream(cls, srcFile), destFile);
	}
	public static void copyFile(InputStream input, File destFile) throws IOException {
		byte[] buffer = new byte[1024];
		int len = 0;
		FileOutputStream output = null;
		try {
			if (input != null) {
				if(!destFile.exists()) {
					destFile.createNewFile();
				}
				output = new FileOutputStream(destFile);
			}
			while (len >= 0) {
				len = input.read(buffer);
				if (len >= 0) {
					output.write(buffer, 0, len);
				}
			}
		} catch (Exception ex) {
			System.out.println("File Copy Failed: " + destFile); ex.printStackTrace();
		}
		finally {
			if (input != null) {
				input.close();
			}
			if (output != null) {
				output.close();
			}
		}
	}
	public static void write(Object obj, OutputStream os) throws IOException {
		int idx = JavaIO.getClassIndex(obj);
		os.write(JavaIO.convertToBytes((short)idx));
		if (idx < 0) {
			return;
		}
		switch (idx) {
			case 0: os.write(JavaIO.convertToBytes((Boolean)obj)); return;
			case 1: os.write(JavaIO.convertToBytes((Character)obj)); return;
			case 2: os.write((Byte)obj); return;
			case 3: os.write(JavaIO.convertToBytes((Short)obj)); return;
			case 4: os.write(JavaIO.convertToBytes((Integer)obj)); return;
			case 5: os.write(JavaIO.convertToBytes((Long)obj)); return;
			case 6: os.write(JavaIO.convertToBytes((Float)obj)); return;
			case 7: os.write(JavaIO.convertToBytes((Double)obj)); return;
			default: JavaIO.writeString(os, obj.toString()); return;
		}
	}
	private static int getClassIndex(Object obj) {
		if (obj != null) {
			for (int c = 0; c < primatives.length; c++) {
				if (obj.getClass().equals(primatives[c])) {
					return c;
				}
			}
		}
		return -1;
	}
	public static Object readObject(InputStream is) throws IOException {
		int idx = JavaIO.readShort(is);
		if (idx < 0) {
			return null;
		}
		switch (idx) {
			case 0: return JavaIO.readBoolean(is);
			case 1: return (char)JavaIO.readShort(is);
			case 2: return (byte)is.read();
			case 3: return JavaIO.readShort(is);
			case 4: return JavaIO.readInt(is);
			case 5: return JavaIO.readLong(is);
			case 6: return JavaIO.readFloat(is);
			case 7: return JavaIO.readDouble(is);
			default: return JavaIO.readString(is);
		}
	}
	public static void writeObjects(OutputStream os, Object[] objArray) throws IOException {
		JavaIO.writeShort(os, (short)objArray.length);
		for (Object obj : objArray) {
			JavaIO.write(obj, os);
		}
	}
	public static Object[] readObjects(InputStream is) throws IOException {
		Object[] objArray = new Object[JavaIO.readShort(is)];
		for (int i = 0; i < objArray.length; i++) {
			objArray[i] = JavaIO.readObject(is);
		}
		return objArray;
	}
	public static <K, V> void write(HashMap<K, V> map, OutputStream os)  throws Exception {
		ParameterizedType parameterizedType = (ParameterizedType)map.getClass().getGenericSuperclass();
		Class keyClass = (Class)parameterizedType.getActualTypeArguments()[0];
		Class valueClass = (Class)parameterizedType.getActualTypeArguments()[1];
		writeArray(map.keySet(), keyClass, os);
		writeArray(map.values(), valueClass, os);
	}
	public static void writeArray(Collection set, Class objClass, OutputStream os) throws Exception {
		if (objClass.equals(String.class)) {
			String keys = "";
			int count = 0;
			for (Object obj : set) {
				if (count == 0) {
					keys = obj.toString();
				} else {
					keys = keys + ";" + obj.toString();
				}
				count++;
			}
			os.write(JavaIO.convertToBytes(keys.length()));
			os.write((byte)0);
			os.write(keys.getBytes("UTF8"));
			return;
		}
		if (objClass.isPrimitive()) {
			os.write(JavaIO.convertToBytes(set.size()));
			if (objClass.equals(Integer.TYPE)) {
				os.write((byte)1);
				for (Object obj : set) {
					os.write(JavaIO.convertToBytes((Integer)obj));
				}
				return;
			}
			if (objClass.equals(Short.TYPE)) {
				os.write((byte)2);
				for (Object obj : set) {
					os.write(JavaIO.convertToBytes((Short)obj));
				}
				return;
			}
			if (objClass.equals(Float.TYPE)) {
				os.write((byte)3);
				for (Object obj : set) {
					os.write(JavaIO.convertToBytes((Float)obj));
				}
				return;
			}
			if (objClass.equals(Double.TYPE)) {
				os.write((byte)4);
				for (Object obj : set) {
					os.write(JavaIO.convertToBytes((Double)obj));
				}
				return;
			}
			if (objClass.equals(Boolean.TYPE)) {
				os.write((byte)5);
				for (Object obj : set) {
					os.write(JavaIO.convertToBytes((Boolean)obj));
				}
				return;
			}
			if (objClass.equals(Long.TYPE)) {
				os.write((byte)6);
				for (Object obj : set) {
					os.write(JavaIO.convertToBytes((Long)obj));
				}
				return;
			}
			if (objClass.equals(Byte.TYPE)) {
				os.write((byte)7);
				for (Object obj : set) {
					os.write((Byte)obj);
				}
				return;
			}
		}
	}
	public static <K, V> void read(HashMap<K, V> map, InputStream is)  throws Exception {
		ParameterizedType parameterizedType = (ParameterizedType)map.getClass().getGenericSuperclass();
		
		Class keyClass = (Class)parameterizedType.getActualTypeArguments()[0];
		K[] keys = (K[])readArray(keyClass, is);
		
		Class valueClass = (Class)parameterizedType.getActualTypeArguments()[1];
		V[] values = (V[])readArray(valueClass, is);
		
		for (int i = 0; i < keys.length; i++) {
			map.put(keys[i], values[i]);
		}
	}
	public static Object readArray(Class objClass, InputStream is) throws Exception {
		int count = JavaIO.readInt(is);
		byte type = (byte)is.read();
		if (objClass.equals(String.class)) {
			byte[] chars = new byte[count];
			is.read(chars);
			String keys = new String(chars, "UTF8");
			return keys.split(";");
		}
		if (objClass.isPrimitive()) {
			Object values = Array.newInstance(objClass, count);
			if (objClass.equals(Integer.TYPE)) {
				for (int i = 0; i < count; i++) {
					Array.set(values, i, JavaIO.readInt(is));
				}
				return values;
			}
			if (objClass.equals(Short.TYPE)) {
				for (int i = 0; i < count; i++) {
					Array.set(values, i, JavaIO.readShort(is));
				}
				return values;
			}
			if (objClass.equals(Float.TYPE)) {
				for (int i = 0; i < count; i++) {
					Array.set(values, i, JavaIO.readFloat(is));
				}
				return values;
			}
			if (objClass.equals(Double.TYPE)) {
				for (int i = 0; i < count; i++) {
					Array.set(values, i, JavaIO.readDouble(is));
				}
				return values;
			}
			if (objClass.equals(Boolean.TYPE)) {
				for (int i = 0; i < count; i++) {
					Array.set(values, i, JavaIO.readBoolean(is));
				}
				return values;
			}
			if (objClass.equals(Long.TYPE)) {
				for (int i = 0; i < count; i++) {
					Array.set(values, i, JavaIO.readLong(is));
				}
				return values;
			}
			if (objClass.equals(Byte.TYPE)) {
				for (int i = 0; i < count; i++) {
					Array.set(values, i, (byte)is.read());
				}
				return values;
			}
		}
		return new Object[0];
	}
	public static void writeShort(OutputStream outputStream, short value) throws IOException {
		byte[] byteArray = convertToBytes(value);
		outputStream.write(byteArray);
		return;
	}
	public static byte[] convertToBytes(short value) {
		byte[] byteArray = new byte[2];
		byteArray[0] = (byte) (value >> 8);
		byteArray[1] = (byte) value;
		return byteArray;
	}
	public static short readShort(InputStream inputStream) throws IOException {
		byte[] byteArray = new byte[2];
		// Read in the next 2 bytes
		inputStream.read(byteArray);
		short number = convertShortFromBytes(byteArray);
		return number;
	}

	public static short convertShortFromBytes(byte[] byteArray) {
		return convertShortFromBytes(byteArray, 0);
	}

	public static short convertShortFromBytes(byte[] byteArray, int offset) {
		// Convert it to a short
		short number = (short) ((byteArray[offset+1] & 0xFF) + ((byteArray[offset+0] & 0xFF) << 8));
		return number;
	}
	public static void writeInt(OutputStream outputStream, int integer)
            throws IOException {
        byte[] byteArray = convertToBytes(integer);

        outputStream.write(byteArray);

        return;
    }
	public static byte[] convertToBytes(int integer) {
        byte[] byteArray = new byte[4];

        byteArray[0] = (byte) (integer >> 24);
        byteArray[1] = (byte) (integer >> 16);
        byteArray[2] = (byte) (integer >> 8);
        byteArray[3] = (byte) integer;
        return byteArray;
    }
	public static int readInt(InputStream inputStream) throws IOException {
        byte[] byteArray = new byte[4];

        // Read in the next 4 bytes
        inputStream.read(byteArray);

        int number = convertIntFromBytes(byteArray);

        return number;
    }

    public static int convertIntFromBytes(byte[] byteArray) {
        return convertIntFromBytes(byteArray, 0);
    }
    
    public static int convertIntFromBytes(byte[] byteArray, int offset) {
        // Convert it to an int
        int number = ((byteArray[offset] & 0xFF) << 24)
                + ((byteArray[offset+1] & 0xFF) << 16) + ((byteArray[offset+2] & 0xFF) << 8)
                + (byteArray[offset+3] & 0xFF);
        return number;
    }
	public static void writeLong(OutputStream outputStream, long value)
            throws IOException {
        byte[] byteArray = convertToBytes(value);

        outputStream.write(byteArray);

        return;
    }

    public static byte[] convertToBytes(long n) {
        byte[] bytes = new byte[8];

        bytes[7] = (byte) (n);
        n >>>= 8;
        bytes[6] = (byte) (n);
        n >>>= 8;
        bytes[5] = (byte) (n);
        n >>>= 8;
        bytes[4] = (byte) (n);
        n >>>= 8;
        bytes[3] = (byte) (n);
        n >>>= 8;
        bytes[2] = (byte) (n);
        n >>>= 8;
        bytes[1] = (byte) (n);
        n >>>= 8;
        bytes[0] = (byte) (n);

        return bytes;
    }
	public static long readLong(InputStream inputStream) throws IOException {
        byte[] byteArray = new byte[8];

        // Read in the next 8 bytes
        inputStream.read(byteArray);

        long number = convertLongFromBytes(byteArray);

        return number;
    }

    public static long convertLongFromBytes(byte[] bytes) {
        return convertLongFromBytes(bytes, 0);
    }

    public static long convertLongFromBytes(byte[] bytes, int offset) {
        // Convert it to an long
        return    ((((long) bytes[offset+7]) & 0xFF) 
                + ((((long) bytes[offset+6]) & 0xFF) << 8)
                + ((((long) bytes[offset+5]) & 0xFF) << 16)
                + ((((long) bytes[offset+4]) & 0xFF) << 24)
                + ((((long) bytes[offset+3]) & 0xFF) << 32)
                + ((((long) bytes[offset+2]) & 0xFF) << 40)
                + ((((long) bytes[offset+1]) & 0xFF) << 48) 
                + ((((long) bytes[offset+0]) & 0xFF) << 56));
    }
	public static void writeDouble(OutputStream outputStream, double value)
            throws IOException {
        byte[] byteArray = convertToBytes(value);

        outputStream.write(byteArray);

        return;
    }

    public static byte[] convertToBytes(double n) {
        long bits = Double.doubleToLongBits(n);
        return convertToBytes(bits);
    }
	public static double readDouble(InputStream inputStream) throws IOException {
        byte[] byteArray = new byte[8];

        // Read in the next 8 bytes
        inputStream.read(byteArray);

        double number = convertDoubleFromBytes(byteArray);

        return number;
    }

    public static double convertDoubleFromBytes(byte[] bytes) {
        return convertDoubleFromBytes(bytes, 0);
    }

    public static double convertDoubleFromBytes(byte[] bytes, int offset) {
        // Convert it to a double
        long bits = convertLongFromBytes(bytes, offset);
        return Double.longBitsToDouble(bits);
    }
	public static void writeFloat(OutputStream outputStream, float fVal)
            throws IOException {
        byte[] byteArray = convertToBytes(fVal);

        outputStream.write(byteArray);

        return;
    }

    public static byte[] convertToBytes(float f) {
        int temp = Float.floatToIntBits(f);
        return convertToBytes(temp);
    }
	public static float readFloat(InputStream inputStream) throws IOException {
        byte[] byteArray = new byte[4];

        // Read in the next 4 bytes
        if (inputStream.read(byteArray) != 4) {
			throw new IOException("incorrect number of bytes for readFloat");
		}

        float number = convertFloatFromBytes(byteArray);

        return number;
    }

    public static float convertFloatFromBytes(byte[] byteArray) {
        return convertFloatFromBytes(byteArray, 0); 
    }
    public static float convertFloatFromBytes(byte[] byteArray, int offset) {
        // Convert it to an int
        int number = convertIntFromBytes(byteArray, offset);
        return Float.intBitsToFloat(number);
    }
	public static void writeBoolean(OutputStream outputStream, boolean bVal)
            throws IOException {
        byte[] byteArray = convertToBytes(bVal);

        outputStream.write(byteArray);

        return;
    }

    public static byte[] convertToBytes(boolean b) {
        byte[] rVal = new byte[1];
        rVal[0] = b ? (byte)1 : (byte)0;
        return rVal;
    }
	public static boolean readBoolean(InputStream inputStream) throws IOException {
        byte[] byteArray = new byte[1];

        // Read in the next byte
        byteArray[0] = (byte)inputStream.read();

        return convertBooleanFromBytes(byteArray);
    }

    public static boolean convertBooleanFromBytes(byte[] byteArray) {
        return convertBooleanFromBytes(byteArray, 0); 
    }
    public static boolean convertBooleanFromBytes(byte[] byteArray, int offset) {
        return byteArray[offset] != 0;
    }

	public static void writeString(OutputStream outputStream, String textVal)
            throws IOException {
        byte[] byteArray = convertToBytes(textVal);

		writeShort(outputStream, (short)byteArray.length);
        outputStream.write(byteArray);

        return;
    }

    public static byte[] convertToBytes(String text) throws IOException {
		return text.getBytes("UTF8");
    }
	public static String readString(InputStream inputStream) throws IOException {
        byte[] byteArray = new byte[readShort(inputStream)];

        // Read in the next byte
        //inputStream.read(byteArray);
		for (int i = 0; i < byteArray.length; i++) {
			byteArray[i] = (byte)inputStream.read();
		}

        return convertStringFromBytes(byteArray);
    }

    public static String convertStringFromBytes(byte[] byteArray) throws IOException {
        return new String(byteArray, "UTF8");
    }
	public static boolean saveXML(File sFile, XMLObject[] objects) {
		if (objects.length == 0) {
			try {
				return sFile.delete();
			} catch (Exception ex) {
				return false;
			}
		}
		File tempFile = null;
		BufferedOutputStream out = null;
		boolean saveExists = false;
		try {
			// get a temp file
			tempFile = File.createTempFile(sFile.getName(), null);
			// delete it, otherwise you cannot rename your existing zip to it.
			tempFile.delete();
			
			saveExists = sFile.exists();
			if (saveExists) {
				boolean renameOk = sFile.renameTo(tempFile);
				if (!renameOk) {
					throw new RuntimeException("could not rename the file " + sFile.getAbsolutePath() + " to " + tempFile.getAbsolutePath());
				}
			}
			FileOutputStream dest = new FileOutputStream(sFile);
			out = new BufferedOutputStream(dest);
			XMLFileUtility.saveXMLObjects(out, objects);
			out.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			try {
				if (saveExists) {
					// Restore original file if an error occurs during save
					tempFile.renameTo(sFile);
				}
			} catch (Exception ex) {}
		} finally {
			try {
				out.close();
			} catch (Exception ex) {}
		}
		return false;
	}
}