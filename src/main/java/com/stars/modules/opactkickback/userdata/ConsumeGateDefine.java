package com.stars.modules.opactkickback.userdata;

public class ConsumeGateDefine {
	private final int id;
	private int consume;
	private int dropId;

	public ConsumeGateDefine(int id, int dropId, int consume) {
		this.id = id;
		this.dropId = dropId;
		this.consume = consume;
	}

	public static ConsumeGateDefine parse(int id, String reward) {
		String[] ss = reward.split("\\+");
		if (ss.length != 2) {
			throw new RuntimeException("ConsumeGateDefine.parse reward  is error! params length must 2");
		}
		int consume = Integer.parseInt(ss[0]);
		int dropId = Integer.parseInt(ss[1]);
		return new ConsumeGateDefine(id, dropId, consume);
	}

	public int getId() {
		return id;
	}

	public void setConsume(int consume) {
		this.consume = consume;
	}

	public int getConsume() {
		return consume;
	}

	public int getDropId() {
		return dropId;
	}

	public void setDropId(int dropId) {
		this.dropId = dropId;
	}

}
