package com.stars.http.net;

import com.stars.util.LogUtil;
import com.stars.util.StringUtil;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created with IntelliJ IDEA.
 * User: yushan
 * Date: 13-2-28
 * Time:
 * http连接
 */
public class HttpConn {

    private String url;

    private HttpURLConnection httpConn;

    private DataOutputStream dos;

    private DataInputStream dis;

    private String responseStr;

    private boolean isError;//是否发生异常

    private int timeout = 2000;
    
    private String contentType;

    public HttpConn(String url) {
        this.url = url;
    }

    public boolean isError() {
        return isError;
    }

    public void get(String requsetStr) {
        sendRequest(requsetStr, "GET");
    }

    public void post(String requsetStr) {
        sendRequest(requsetStr, "POST");
    }

    public void get(byte[] requsetBytes) {
        sendRequest(requsetBytes, "GET");
    }

    public void post(byte[] requsetBytes) {
        sendRequest(requsetBytes, "POST");
    }
    
    public void setContentType(String contentType){
    	this.contentType = contentType;
    }


    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    /**
     * 发送http请求
     *
     * @param requsetStr
     * @param method
     */
    private void sendRequest(String requsetStr, String method) {
        try {
            URL u = new URL(url);
            httpConn = (HttpURLConnection) u.openConnection();
            httpConn.setDoOutput(true);
            httpConn.setDoInput(true);
            httpConn.setUseCaches(false);
            httpConn.setRequestMethod(method);
            httpConn.setConnectTimeout(timeout);
            httpConn.setReadTimeout(timeout);
            if(StringUtil.isNotEmpty(contentType)){
            	httpConn.setRequestProperty("Content-Type", contentType);
            }
            dos = new DataOutputStream(httpConn.getOutputStream());
            dos.write(requsetStr.getBytes());
            dos.flush();
            dos.close();
            getResponse();
        } catch (IOException e) {
            isError = true;
            com.stars.util.LogUtil.error("sendRequest IOException|"+ url+"|"+requsetStr, e);
        } catch (Exception e) {
            isError = true;
            com.stars.util.LogUtil.error("sendRequest Exception|"+ url+"|"+requsetStr, e);
        }
    }

    /**
     * 发送http请求
     *
     * @param requsetBytes
     * @param method
     */
    private void sendRequest(byte[] requsetBytes, String method) {
        try {
            URL u = new URL(url);
            httpConn = (HttpURLConnection) u.openConnection();
            httpConn.setDoOutput(true);
            httpConn.setDoInput(true);
            httpConn.setUseCaches(false);
            httpConn.setRequestMethod(method);
            httpConn.setConnectTimeout(timeout);
            httpConn.setReadTimeout(timeout);
            if(StringUtil.isNotEmpty(contentType)){
            	httpConn.setRequestProperty("Content-Type", contentType);
            }
            dos = new DataOutputStream(httpConn.getOutputStream());
            dos.write(requsetBytes);
            dos.flush();
            dos.close();
            getResponse();
        } catch (IOException e) {
            isError = true;
            com.stars.util.LogUtil.error("sendRequest1", e.getMessage(), e);
        } catch (Exception e) {
            isError = true;
            LogUtil.error("sendRequest2", e.getMessage(), e);
        }
    }

    /**
     * 解析数据
     *
     * @throws IOException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    private void getResponse() throws IOException, IllegalAccessException, InstantiationException {
        int code = httpConn.getResponseCode();
        if (code == 200) {
            //打开一个链接
            dis = new DataInputStream(httpConn.getInputStream());
            byte[] data = new byte[dis.available()];
            dis.read(data);
            this.responseStr = new String(data, "UTF-8");
            dis.close();
        }
    }

    public void close() {
        if (httpConn != null) {
            httpConn.disconnect();
        }
    }

    public String getResponseStr() {
        return responseStr;
    }
}
