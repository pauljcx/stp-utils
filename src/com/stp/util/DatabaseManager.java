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
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.SimpleDateFormat;

/** @author Paul Collins
 *  @version v1.0 ~ 03/10/2018
 *  HISTORY: Version 1.0 created a singleton class to retrieve data from a MySQL database using the jdbc driver ~ 03/10/2018
 */
public final class DatabaseManager {
	private static final Logger logger = Logger.getLogger(Configuration.class.getName());
	public static final long serialVersionUID = 1L;
	private static final Map<String, Class<?>> resultSetMap = new HashMap<String, Class<?>>();
	private static final Map<Class<?>, Class<?>> primMap = new HashMap<Class<?>, Class<?>>();
	private static final Map<Class<?>, Integer> paramMap = new HashMap<Class<?>, Integer>();
	static {
		try {
			resultSetMap.put(Types.INTEGER + "", Integer.TYPE);
			resultSetMap.put(Types.TINYINT + "", Integer.TYPE);
	        resultSetMap.put(Types.DOUBLE + "", Double.TYPE);
	        resultSetMap.put(Types.CHAR + "", Character.TYPE);
			resultSetMap.put(Types.BOOLEAN + "", Boolean.TYPE);
			resultSetMap.put(Types.VARCHAR + "", Class.forName("java.lang.String"));
			resultSetMap.put(Types.DATE + "", Class.forName("java.sql.Date"));
			resultSetMap.put(Types.TIMESTAMP + "", Class.forName("java.sql.Timestamp"));
			resultSetMap.put(Types.DECIMAL + "", Float.TYPE);
			resultSetMap.put(Types.FLOAT + "", Float.TYPE);
			primMap.put(Class.forName("java.lang.Byte"), Byte.TYPE);
			primMap.put(Class.forName("java.lang.Short"), Short.TYPE);
			primMap.put(Class.forName("java.lang.Integer"), Integer.TYPE);
			primMap.put(Class.forName("java.lang.Long"), Long.TYPE);
	        primMap.put(Class.forName("java.lang.Float"), Float.TYPE);
			primMap.put(Class.forName("java.lang.Double"), Double.TYPE);
			primMap.put(Class.forName("java.lang.Boolean"), Boolean.TYPE);
			primMap.put(Class.forName("java.lang.Character"), Character.TYPE);
			primMap.put(Class.forName("java.sql.Date"), Class.forName("java.util.Date"));
			paramMap.put(Integer.TYPE, 0);
	        paramMap.put(Double.TYPE, 1);
			paramMap.put(Float.TYPE, 2);
	        paramMap.put(Character.TYPE, 3);
			paramMap.put(Boolean.TYPE, 4);
			paramMap.put(Class.forName("java.lang.String"), 5);
			paramMap.put(Class.forName("java.sql.Date"), 6);
			paramMap.put(Class.forName("java.sql.Timestamp"), 7);
		} catch (Exception ex) {
			logger.log(Level.WARNING, "Unable to initialize database class maps: " + ex.getMessage());
		}
	}
	//private static final String USER = "jdkuser";
	//private static final String PASS = "jdkpass";
	//private static final String SQL_LOC = "jdbc:mysql://localhost:3306/";
	private static Connection connection;
	private static Statement stmt;
	private static boolean connected = false;
	private static boolean logging = true;
	private static ArrayList<String> querylog = new ArrayList<String>();
	private static String url = null;
	private static String[] credentials = null;
	
	public static void setCredentials(String database, String location, String username, String password) {
		url = "jdbc:mysql://" + location + "/" + database;
		credentials = new String[] { username, password };
	}

	public static boolean setConnection(String[] cValues, boolean flushlog) {
		if (cValues.length < 4) {
			return false;
		} else {
			return setConnection(cValues[0], cValues[1], cValues[2], cValues[3], flushlog);
		}
	}
	public static boolean setConnection(String database, String location, String username, String password, boolean flushlog) {
		url = "jdbc:mysql://" + location + "/" + database;
		credentials = new String[] { username, password };
		connected = reconnect();
		if (connected && flushlog) {
			for (int q = 0; q < querylog.size(); q++) {
				executeStatement(querylog.get(q).toString());
			}
		}
		if (connected) {
			querylog.clear();
		}
		return connected;
	}
	public static String getConnectionUrl() {
		return url;
	}
	public static boolean isConnected() {
		return connected;
	}
	public static void closeConnection() {
		try {
			connection.close();
			connection = null;
			connected = false;
		} catch (Exception e) {
		}
		querylog.clear();
	}
	public static boolean reconnect() {
		if (url != null && credentials != null) {
			try {
				closeConnection();
				if (connection == null || connection.isClosed()) {
					Class.forName("com.mysql.jdbc.Driver");
					connection = DriverManager.getConnection(url, credentials[0], credentials[1]);
					stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
					connected = true;
					return true;
				} else {
					connected = true;
					return true;
				}
			} catch (SQLException ex) {
				logger.log(Level.WARNING, "Database Connection Error: [" + ex.getErrorCode() + "] " + ex.getMessage());
				return false;
			} catch (ClassNotFoundException cnfe) {
				logger.log(Level.WARNING, "Unable to locate database driver.");
				return false;
			}
		} else {
			return false;
		}
	}
	private static void executeStatement(String command) {
		if (connected) {
			try {
				stmt.executeUpdate(command);
			} catch (Exception ex)	{
				logger.log(Level.WARNING, "Failed to execute MySQL statement: " + command + " with error message " + ex.getMessage());
				closeConnection();
				if (logging) {
					logQuery(command);
				}
			}
		} else if (logging) {
			logQuery(command);
		}
		logging = true;
	}
	public static int executeStatement(String command, Object[] params) {
		if (connected) {
			try { 
				PreparedStatement statement = connection.prepareStatement(command);
				for (int p = 0; p < params.length; p++) {
					Integer s = paramMap.get(params[p].getClass());
					s = (s == null) ? 5 : s;
					switch(s) {
						case 0: statement.setInt(p+1, (Integer)params[p]); break;
						case 7: statement.setTimestamp(p+1, (Timestamp)params[p]); break;
						default: statement.setString(p+1, params[p].toString()); break;
					}
				}
				return statement.executeUpdate();
			} catch (Exception ex) {
				logger.log(Level.WARNING, "Failed to execute MySQL statement: " + command + " with error message " + ex.getMessage());
			}
		}
		return -1;
	}
	public static void addTable(String tableName, String[] args) {
		String command = "CREATE TABLE " + tableName + "(id INT NOT NULL AUTO_INCREMENT,";
		for (int i = 0; i < args.length; i++) {
			command = command + args[i];
			if (i != args.length - 1)
			{ command = command + ","; }
		}
		command = command + ",PRIMARY KEY (id))";
		executeStatement(command);
	}
	public static void truncateTable(String tableName) {
		executeStatement("TRUNCATE TABLE " + tableName);
	}
	public static void removeTable(String tableName) {
		executeStatement("DROP TABLE " + tableName);
	}
	public static Object addRow(String tableName, Object[] args) throws Exception {
		int columns = 0;
		ResultSet rs = null;
		try {
			rs = stmt.executeQuery("SELECT * FROM " + tableName);
			columns = rs.getMetaData().getColumnCount();
		} catch (Exception ex) {
		}
		if (columns == 0 || columns == args.length) {
			Object row = 0;
			String command = "INSERT INTO " + tableName + " VALUES(";
			for (int i = 0; i < args.length; i++) {
				if (args[i] instanceof Boolean) {
					command = command + args[i].toString();
				} else {
					command = command + "'" + args[i].toString().replace("'", ":sqt;") + "'";
				}
				if (i != args.length - 1) {
					command = command + ",";
				}
			}
			command = command + ")";
			executeStatement(command);
			if (rs != null) {
				rs.close();
				rs = stmt.executeQuery("SELECT id FROM " + tableName);
				rs.last();
				row = rs.getObject("id", resultSetMap);
			}
			return row; 
		} else {
			throw new Exception("Wrong number of arguments.");
		}
	}	
	public static void deleteRow(String tableName, String validation) {
		executeStatement("DELETE FROM " + tableName + " WHERE " + validation);
	}
	public static void addColumn(String tableName, String arg) {
		executeStatement("ALTER TABLE " + tableName + " ADD " + arg);
	}
	public static void alterColumn(String tableName, String columnName, String arg) {
		executeStatement("ALTER TABLE " + tableName + " CHANGE " + columnName + " " + arg);
	}
	public static void deleteColumn(String tableName, String columnName) {
		executeStatement("ALTER TABLE " + tableName + " DROP " + columnName);
	}
	public static Object updateField(String tableName, String columnName, Object value, String validation) {
		if (value instanceof Boolean || value instanceof Number) {
			executeStatement("UPDATE " + tableName + " SET " + columnName + " = " + value + " WHERE " + validation);
		} else if (value instanceof java.util.Date) {
			String text = new SimpleDateFormat("yyyy-MM-dd").format(value);
			executeStatement("UPDATE " + tableName + " SET " + columnName + " = '" + text + "' WHERE " + validation);
		} else {
			executeStatement("UPDATE " + tableName + " SET " + columnName + " = '" + value.toString().replace("'", ":sqt;") + "' WHERE " + validation);
		}
		return value;
	}
	public static Object updateField(String tableName, String columnName, Object value, String validation, String[] params) {
		if (value instanceof Boolean || value instanceof Number) {
			executeStatement("UPDATE " + tableName + " SET " + columnName + " = " + value + " WHERE " + validation, params);
		} else if (value instanceof java.util.Date) {
			String text = new SimpleDateFormat("yyyy-MM-dd").format(value);
			executeStatement("UPDATE " + tableName + " SET " + columnName + " = '" + text + "' WHERE " + validation, params);
		} else {
			executeStatement("UPDATE " + tableName + " SET " + columnName + " = '" + value.toString().replace("'", ":sqt;") + "' WHERE " + validation, params);
		}
		return value;
	}
	public static Object[] getColumnValues(String tableName, String columnName) throws Exception {
		Object[] values = null;
		ResultSet rs = stmt.executeQuery("SELECT " + columnName + " FROM " + tableName);
		rs.last();
		values = new Object[rs.getRow()];
		rs.beforeFirst();
		int rw = 0;
		while(rs.next()) {
			values[rw] = rs.getObject(columnName, resultSetMap);
			rw++;
		}
		return values;
	}
	public static void viewTable(String tableName) throws Exception {
		//ResultSet rs = connection.getMetaData().getTables(null, null, null, null);
		ResultSet rs = stmt.executeQuery("SELECT * FROM " + tableName);
		ResultSetMetaData rsMetaData = rs.getMetaData();
		int columns = rsMetaData.getColumnCount();
		for (int c = 1; c <= columns; c++) {
			System.out.print(rsMetaData.getColumnName(c));
			if (c != columns) {
				System.out.print(" : ");
			}
		}
		System.out.print("\n");
		while(rs.next()) {
			for (int i = 1; i <= columns; i++) {
				System.out.print(rs.getString(i));
				if (i != columns) {
					System.out.print(" : ");
				} else {
					System.out.print("\n");
				}
			}
		}
	}
	public static Object getValueFromTable(String tableName, String columnName, String validation) throws Exception {
		String cmd = "SELECT " + columnName + " FROM " + tableName + " WHERE " + validation;
		Object obj = null;
		ResultSet rs = stmt.executeQuery(cmd);
		if (rs.first()) {
			obj = rs.getObject(columnName, resultSetMap);
			if (obj instanceof String) {
				obj = ((String)obj).replace(":sqt;", "'");
			}
		}
		return obj;
	}
	public static Object getValueFromTable(String tableName, String columnName, String validation, String[] params) throws Exception {
		Object obj = null;
		PreparedStatement stmt = connection.prepareStatement("SELECT " + columnName + " FROM " + tableName + " WHERE " + validation);
		for (int p = 0; p < params.length; p++) {
			stmt.setString(p+1, params[p]);
		}
		stmt.execute();
		ResultSet rs = stmt.getResultSet();
		if (rs.first()) {
			obj = rs.getObject(columnName, resultSetMap);
			if (obj instanceof String) {
				obj = ((String)obj).replace(":sqt;", "'");
			}
		}
		return obj;
	}
	public static Object[][] getValuesFromTable(String tableName, String[] args, String validation) throws Exception {
		Object[][] values = null;
		ResultSet rs = stmt.executeQuery("SELECT " + compileString(args) + " FROM " + tableName + " WHERE " + validation);
		ResultSetMetaData metaData = rs.getMetaData();
		rs.last();
		values = new Object[rs.getRow()][metaData.getColumnCount()];
		rs.beforeFirst();
		int rw = 0;
		while(rs.next()) {
			for (int i = 1; i <= values[0].length; i++) {
				values[rw][i-1] = rs.getObject(metaData.getColumnName(i), resultSetMap);
				if (values[rw][i-1] instanceof String) {
					values[rw][i-1] = ((String)values[rw][i-1]).replace(":sqt;", "'");
				}
			}
			rw++;
		}
		return values;
	}
	public static Object getObjectsFromTable(Class<?> classType, String tableName, String[] args, String validation) throws Exception {
		ResultSet rs = stmt.executeQuery("SELECT " + compileString(args) + " FROM " + tableName + " WHERE " + validation);
		ResultSetMetaData metaData = rs.getMetaData();
		rs.last();
		Object objects = Array.newInstance(classType, rs.getRow());
		Object[] values = new Object[metaData.getColumnCount()];
		rs.beforeFirst();
		int rw = 0;
		Constructor<?> constructor;
		while(rs.next()) {
			for (int i = 1; i <= values.length; i++) {
				values[i-1] = rs.getObject(metaData.getColumnName(i), resultSetMap);
				if (values[i-1] instanceof String) {
					values[i-1] = ((String)values[i-1]).replace(":sqt;", "'");
				}
			}
			constructor = classType.getConstructor(getObjectClasses(values));
			Array.set(objects, rw, classType.cast(constructor.newInstance(values)));
			rw++;
		}
		return objects;
	}
	public static Object getObjectFromTable(Class<?> classType, String tableName, String[] args, String validation, String[] params) throws Exception {
		PreparedStatement stmt = connection.prepareStatement("SELECT " + compileString(args) + " FROM " + tableName + " WHERE " + validation);
		for (int p = 0; p < params.length; p++) {
			stmt.setString(p+1, params[p]);
		}
		stmt.execute();
		ResultSet rs = stmt.getResultSet();
		ResultSetMetaData metaData = rs.getMetaData();
		rs.last();
		Object obj = null;
		Object[] values = new Object[metaData.getColumnCount()];
		if (rs.first()) {
			for (int i = 1; i <= values.length; i++) {
				values[i-1] = rs.getObject(metaData.getColumnName(i), resultSetMap);
				if (values[i-1] instanceof String) {
					values[i-1] = ((String)values[i-1]).replace(":sqt;", "'");
				}
			}
			Constructor<?> constructor = classType.getConstructor(getObjectClasses(values));
			obj = classType.cast(constructor.newInstance(values));
		}
		return obj;
	}
	public static Object[][] getValuesFromTable(String tableName, String[] args, String validation, String[] params) throws Exception {
		Object[][] values = null;
		PreparedStatement stmt = connection.prepareStatement("SELECT " + compileString(args) + " FROM " + tableName + " WHERE " + validation);
		for (int p = 0; p < params.length; p++) {
			stmt.setString(p+1, params[p]);
		}
		stmt.execute();
		ResultSet rs = stmt.getResultSet();
		
		ResultSetMetaData metaData = rs.getMetaData();
		rs.last();
		values = new Object[rs.getRow()][metaData.getColumnCount()];
		rs.beforeFirst();
		int rw = 0;
		while(rs.next()) {
			for (int i = 1; i <= values[0].length; i++) {
				values[rw][i-1] = rs.getObject(metaData.getColumnName(i), resultSetMap);
				if (values[rw][i-1] instanceof String) {
					values[rw][i-1] = ((String)values[rw][i-1]).replace(":sqt;", "'");
				}
			}
			rw++;
		}
		return values;
	}
	private static String compileString(String[] args) {
		String str = "";
		for (int i = 0; i < args.length; i++) {
			if (i != args.length - 1) {
				str = str + args[i] + ", ";
			} else {
				str = str + args[i];
			}
		}
		return str;
	}
	public static Class<?>[] getObjectClasses(Object[] objects) {
		Class<?>[] classes = new Class<?>[objects.length];
		Class primClass;
		for (int i = 0; i < objects.length; i++) {
			primClass = primMap.get(objects[i].getClass());
			if (primClass != null) {
				classes[i] = primClass;
			} else {
				classes[i] = objects[i].getClass();
			}
		}
		return classes;
	}
	private static void logQuery(String query) {
		querylog.add(query);
	}
	public static Object[] getQueryLog() {
		return querylog.toArray();
	}
	public static void addQuerys(Object[] querys) {
		for (int q = 0; q < querys.length; q++) {
			logQuery(querys[q].toString());
		}
	}
	public static void skipLogging() {
		logging = false;
	}
}
