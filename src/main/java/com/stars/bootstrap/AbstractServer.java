package com.stars.bootstrap;


/**
 * Created by jx on 2015/2/27.
 */
public abstract class AbstractServer implements Server {

    public static final byte OFF = 0;
    public static final byte ON = 1;

    private long startTimestamp; // 启动时间
    private byte state = OFF; // 状态

    private String name;
    
    private BootstrapConfig config;

    public AbstractServer(String name) {
        this.name = name;
    }

    //设置状态
    private void setState(byte state) {
        switch (state) {
            case OFF:
            case ON:
                this.state = state;
                return;
            default:
                throw new IllegalArgumentException();
        }
    }

    //设置启动时状态
    public void setOn() {
        setState(ON);
    }

    public void setOff() {
        setState(OFF);
    }

    // 设置启动时间
    public void setStartTimestamp() {
        this.startTimestamp =  System.currentTimeMillis();
    }

    public long getStartTimestamp() {
        return startTimestamp;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isOn() {
        return state == ON;
    }

    @Override
    public boolean isOff() {
        return state == OFF;
    }

	public BootstrapConfig getConfig() {
		return config;
	}

	public void setConfig(BootstrapConfig config) {
		this.config = config;
	}
}
