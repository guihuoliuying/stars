package com.stars.util.backdoor;

import com.stars.util.backdoor.result.BackdoorResult;

import java.io.BufferedReader;
import java.io.PrintWriter;

public class BackdoorContext {
	
	private com.stars.util.backdoor.Backdoor backdoor;
	private String inputCommand;
	private PrintWriter pw;
	private BufferedReader br;
    private BackdoorResult lastCommandResult;

	private BackdoorContext() {

	}

	public static BackdoorContext newInstance() {
		BackdoorContext context = new BackdoorContext();
		context.inputCommand = "";
		context.br = null;
		context.pw = null;
		return context;
	}

	public void setInputCommand(String inputCommand) {
		this.inputCommand = inputCommand;
	}

	public String getInputCommand() {
		return this.inputCommand;
	}

	public void setBufferedReader(BufferedReader br) {
		this.br = br;
	}

	public BufferedReader getBufferedReader() {
		return this.br;
	}

	public void setPrintWriter(PrintWriter pw) {
		this.pw = pw;
	}

	public PrintWriter getPrintWriter() {
		return pw;
	}

	public void setBackdoor(com.stars.util.backdoor.Backdoor backdoor) {
		this.backdoor = backdoor;
	}

	public Backdoor getBackdoor() {
		return this.backdoor;
	}

    public void setLastCommandResult(BackdoorResult result) {
        this.lastCommandResult = result;
    }

    public BackdoorResult getLastCommandResult() {
        return lastCommandResult;
    }

}
