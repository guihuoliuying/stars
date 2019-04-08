package com.stars.http.net;

import com.stars.util.LogUtil;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;

/**
 * 阻塞模式HTTP请求
 * 
 * @author momo
 * 
 */
public class HttpConnection {

	private HttpURLConnection httpConn;

	private String address;

	private DataOutputStream dos;

	private DataInputStream dis;
	
	private int timeOut = 1000;

	public HttpConnection() {
	}

	public HttpConnection(String address, int timeOut) {
		this.address = address;
		this.timeOut = timeOut;
	}
	

	public static String PostHttpData(String address, String packet,int serverIndex, int timeOut)
			throws Exception {
		HttpConnection http = new HttpConnection(address, timeOut);
		return http.sendPacket(packet,serverIndex);
	}


	private void useProxy(){
		String strProxy = "";
		String strPort = "";
		Properties systemProperties = System.getProperties(); //关键代码
		systemProperties.setProperty("http.proxySet","true");
		systemProperties.setProperty("http.proxyHost",strProxy);
		systemProperties.setProperty("http.proxyPort",strPort) ;
		//https
		systemProperties.setProperty("https.proxySet","true");
		systemProperties.setProperty("https.proxyHost",strProxy);
		systemProperties.setProperty("https.proxyPort",strPort);
	}
	
	private void openConnection() throws IOException {
		if (address == null || httpConn != null) {
			return;
		}
		URL url = new URL(address);
		httpConn = (HttpURLConnection) url.openConnection();
		useProxy();
		httpConn.setDoOutput(true);
		httpConn.setDoInput(true);
		httpConn.setConnectTimeout(this.timeOut);
		httpConn.setReadTimeout(this.timeOut);
	}

	private void setRequestProperty(int dataLength) throws IOException {
		httpConn.setRequestMethod("POST");
		httpConn.setRequestProperty("Content-Type", String.valueOf(dataLength));
	}

	private String sendPacket(String outPacket,int serverIndex) {
		String data = String.valueOf(serverIndex);
		try {
			openConnection();
			if (outPacket != null) {
				setRequestProperty(outPacket.getBytes().length);
			}
			if(httpConn != null){
				
			}
			postData(outPacket);
			data = readData();
		} catch (Exception e) {
			// e.printStackTrace();
		 	com.stars.util.LogUtil.error("HttpConnFailInfo: " + outPacket, e);
		 	com.stars.util.LogUtil.error("HttpConnection Fail: " , e.getMessage(), e);
		} finally {
			release();
		}
		return data;
	}

	private void release() {
		try {
			if (dis != null) {
				dis.close();
			}
			if (dos != null) {
				dos.close();
			}
			if (httpConn != null) {
				httpConn.disconnect();
			}
		} catch (IOException e) {
			LogUtil.error(
                    "Close HttpConnection Fail: " + e.getMessage(), e);
		}
	}

	private void postData(String outPacket) throws IOException {
		if (outPacket == null) {
			if (httpConn != null) {
				httpConn.disconnect();
			}
			return;
		}
		dos = new DataOutputStream(httpConn.getOutputStream());
		if (dos == null) {
			if (httpConn != null) {
				httpConn.disconnect();
			}
			return;
		}
		dos.write(outPacket.getBytes());
		dos.flush();
	}

	/**
	 * 实现从网络数据流中读取数据
	 * 
	 * @throws IOException
	 */
	private String readData() throws IOException {
		dis = new DataInputStream(httpConn.getInputStream());
		if (dis == null) {
			return "";
		}
		int ch = 0;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		while ((ch = dis.read()) != -1) {
			bos.write(ch);
		}
		String data = new String(bos.toByteArray(), "UTF-8");
		return data;
	}
}
