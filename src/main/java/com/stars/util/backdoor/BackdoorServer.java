package com.stars.util.backdoor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;

/**
 * 用于测试时方便修改配置的值，不需要重启服务
 */
public class BackdoorServer implements Runnable {
	
//	private String hostname;
	private int port;
	
	public BackdoorServer(String hostname, int port) {
//		this.hostname = hostname;
		this.port = port;
	}

    @Override
    public void run() {
        Thread.currentThread().setName("ConsoleListener");
        ServerSocket ss = null;
        try {
            ss = new ServerSocket();
            ss.setSoTimeout(10000);
            ss.bind(new InetSocketAddress(port));
	        while (true) {
	            try {
	                Socket socket = ss.accept();
	                new Thread(new Backdoor(socket)).start();
	            } catch (SocketTimeoutException e) { 
	            	// Just for exit
	            } catch (IOException e) {
	            	System.err.println("控制台线程出现错误");
                    e.printStackTrace();
	            }
	        }
        } catch (Exception e) {
            System.err.println("控制台线程出现错误");
            e.printStackTrace();
        } finally {
        	try { ss.close(); } catch (IOException e) { }
            System.err.println("控制台线程退出");
        }
        
    }
    
    public static void main(String[] args) throws InterruptedException {
    	Thread consoleListener = new Thread(new BackdoorServer("localhost", 52014));
    	System.out.println("Start...");
    	consoleListener.start();
    	TimeUnit.SECONDS.sleep(10);
    	System.out.println("End...");
    }

}
