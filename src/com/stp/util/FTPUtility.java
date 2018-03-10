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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

/** @author Paul Collins
 *  @version v1.0 ~ 03/10/2018
 *  HISTORY: Version 1.0 created a utility class to access and store files via FTP ~ 03/10/2018
 */
public class FTPUtility {
	public static int BUFFER_SIZE = 10240;
	private String host = "unspecified host";
	private String username = "admin";
	private String password = "unknown";
	private Appendable stream;
	private boolean connected;
	
	private final FTPClientConfig config;
	private final FTPClient ftp;
	
	public FTPUtility(Appendable stream) {
		this(stream, "unspecified host", "admin", "unknown");
	}
	public FTPUtility(Appendable stream, String host, String username, String password) {
		this.stream = stream;
		ftp = new FTPClient();
		config = new FTPClientConfig();
        config.setUnparseableEntries(false);
        ftp.configure(config);
		setHost(host);
		setCredentials(username, password);
	}
	public void setHost(String host) {
		this.host = host;
	}
	public void setCredentials(String username, String password) {
		this.username = username;
		this.password = password;
	}
	public boolean open() {
		try {
			ftp.connect(host);
			// After connection attempt, you should check the reply code to verify success.
			int reply = ftp.getReplyCode();
			if (!FTPReply.isPositiveCompletion(reply)) {
				ftp.disconnect();
				appendMessage("FTP Client: server refused connection\n");
				throw new Exception();
			}
			if (!ftp.login(username, password)) {
				ftp.logout();
				appendMessage("FTP Client: User " + username + " login failed\n");
				throw new Exception();
			}
			appendMessage("FTP Client: User " + username + " login success\n");
			ftp.setFileType(FTP.BINARY_FILE_TYPE);
			ftp.enterLocalPassiveMode();
			connected = true;
			return true;
		} catch (Exception ex) {
			 if (ftp.isConnected()) {
                try {
                    ftp.disconnect();
                } catch (IOException f) {}
            }
			appendMessage("FTP Client: could not connect to server");
			connected = false;
			return false;
		}
	}
	public void close() {
		connected = false;
		try {
			ftp.disconnect();
		} catch (Exception ex) {
			appendMessage("FTP Client: " + ex.getMessage() + "\n");
		}
	}
	public void setDirectory(String dir) {
		if (connected)	{
			try {
				if (ftp.changeWorkingDirectory(dir)) {
					appendMessage("FTP Client: set directory /" + dir + "\n");
				} else {
					if (ftp.makeDirectory(dir)) {
						appendMessage("FTP Client: created directory /" + dir + "\n");
						ftp.changeWorkingDirectory(dir);
					}
				}
			} catch (Exception ex) {
				appendMessage("FTP Client: " + ex.getMessage() + "\n");
			}	
		}	
	}
	public void upload(String fileName, File source) {
		if (connected && fileName != null && source != null) {
			InputStream input = null;
			try {
				input = new FileInputStream(source);
				ftp.storeFile(fileName, input);
				appendMessage("FTP Client: " + fileName + " was successfully uploaded\n");
			} catch (Exception ex) {
				appendMessage("FTP Client: " + ex.getMessage() + "\n");
			} finally {
				if (input != null) {
					try {
						input.close();
					} catch (IOException ioe) {
						ioe.printStackTrace();
					}
				}
			}
		}
	}
	public void download(String fileName, File destination) {
		if (connected && fileName != null && destination != null) {
			OutputStream output = null;
			try {
				output = new FileOutputStream(destination);
				ftp.retrieveFile(fileName, output);
				appendMessage("FTP Client: " + fileName + " was successfully downloaded\n");
			} catch (Exception ex) {
				appendMessage("FTP Client: " + ex.getMessage() + "\n" );
			} finally {
				if (output != null) {
					try {
						output.close();
					} catch (IOException ioe) {
						ioe.printStackTrace();
					}
				}
			}
		}
	}
	private void appendMessage(String text) {
		if (stream != null) {
			try {
				stream.append(text);
			} catch (IOException ioe) {
			}
		}
	}
}