package com.stars.util.backdoor;

import com.stars.util.backdoor.command.pipeline.CommandPipeline;
import com.stars.util.backdoor.parser.Lexer;
import com.stars.util.backdoor.parser.Parser;
import com.stars.util.backdoor.result.BackdoorResult;
import com.stars.util.backdoor.view.IView;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.atomic.AtomicInteger;

public class Backdoor implements Runnable {
    
	private static final AtomicInteger counter = new AtomicInteger(0);
    private Socket socket;
    
    public Backdoor(Socket socket) throws SocketException {
        this.socket = socket;
        this.socket.setTcpNoDelay(true);
    }

    public Backdoor(Socket socket, int timeout) throws SocketException {
        this.socket = socket;
        this.socket.setSoTimeout(timeout);
        this.socket.setTcpNoDelay(true);
    }

    @Override
    public void run() {
        Thread.currentThread().setName("Console - " + counter.getAndIncrement());
        BufferedReader br = null;
        PrintWriter pw = null;
        try {
            br = new BufferedReader(
                    new InputStreamReader(
                            socket.getInputStream(), "UTF-8")
            );
            pw = new PrintWriter(
                    new BufferedWriter(
                            new OutputStreamWriter(
                                    socket.getOutputStream(), "UTF-8")
                    )
            );
            printWelcomeInfo(pw);
            // Lock other thread
            println(pw, "lock message thread...");
            println(pw, "lock file scanner...");
            while (true) {
                String line = br.readLine();
                try {
                    // Create context
                    BackdoorContext context = BackdoorContext.newInstance();
                    // Set the console
                    context.setBackdoor(this);
                    // Set the input to context
                    context.setInputCommand(line);
                    // Set the buffered reader to context
                    context.setBufferedReader(br);
                    // Set the print writer to context
                    context.setPrintWriter(pw);
                    // Call syntax parser to generate the pipeline
                    CommandPipeline pipeline = new Parser(new Lexer(line)).program();
                    // Execute
                    pipeline.exec(context);
                    BackdoorResult result = context.getLastCommandResult();
                    // Output result
                    if (null != result
                            && BackdoorResult.TYPE_QUIT == result.getType()) {
                        break;
                    } else if (null != result) {
                        printResult(pw, result);
                    }
                } catch (Exception e) {
                    e.printStackTrace(pw);
                    pw.flush();
                }
            }
            printGoodbyeInfo(pw);
        } catch (Exception e) {
            e.printStackTrace(pw);
            pw.flush();
        } finally {
            try {
                br.close();
            } catch (Exception e) {
            }
            try {
                pw.close();
            } catch (Exception e) {
            }
            try {
                socket.close();
            } catch (Exception e) {
            }
        }
    }
    
    private void printWelcomeInfo(PrintWriter pw) {
    	pw.println("Welcome to stat-scheduler's console");
    	pw.flush();
    }
    
    private void printGoodbyeInfo(PrintWriter pw) {
    	pw.println("Goodbye");
    	pw.flush();
    }
    
    private void printPrompt(PrintWriter pw) {
    	pw.print("console# ");
    	pw.flush();
    }
    
    
    @SuppressWarnings("unused")
	private void print(PrintWriter pw, String message) {
    	pw.print(message);
    	pw.flush();
    }
    
    private void println(PrintWriter pw, String message) {
    	pw.println(message);
    	pw.flush();
    }
    
    public static void printResult(PrintWriter pw, BackdoorResult result) {
    	IView view = result.getView();
    	if (null != view) {
    		pw.print(view.getDisplayedView());
    	}
    	pw.flush();
    }
    
}
