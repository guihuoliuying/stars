package com.stars.server.connector;

/**
 * Created by zws on 2015/9/2.
 */
public class BackendAddress {
    public final int serverId; // 服务ID（0-31）
    public final String ip;
    public final int port;

    public BackendAddress(int serverId, String ip, int port) {
        this.serverId = serverId;
        this.ip = ip;
        this.port = port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BackendAddress that = (BackendAddress) o;

        if (serverId != that.serverId) {
            return false;
        }
        if (port != that.port) {
            return false;
        }
        return ip.equals(that.ip);

    }

    @Override
    public int hashCode() {
        int result = serverId;
        result = 31 * result + ip.hashCode();
        result = 31 * result + port;
        return result;
    }

    @Override
    public String toString() {
        return "(" + serverId + ",'" + ip + "'," + port + ")";
    }

	public int getServerId() {
		return serverId;
	}

	public String getIp() {
		return ip;
	}

	public int getPort() {
		return port;
	}
}
