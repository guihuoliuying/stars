package com.stars.modules.scene.util;

import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.util.LogUtil;

/**
 * 
 * @author xieyuejun
 *
 *         三维坐标
 *
 */
public class ThreeCoord implements Cloneable {
	private int x;
	private int y;
	private int z;

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getZ() {
		return z;
	}

	public void setZ(int z) {
		this.z = z;
	}

	// 输入格式 [x+y+z]
	public static ThreeCoord parseCoord(String coStr) {
		String oldCoStr = coStr;
		ThreeCoord co = new ThreeCoord();
		try {
			coStr = coStr.replace("[", "");
			coStr = coStr.replace("]", "");
			String[] strs = coStr.split("\\+");
			co.setX(Integer.parseInt(strs[0]));
			co.setY(Integer.parseInt(strs[1]));
			co.setZ(Integer.parseInt(strs[2]));
		} catch (Exception e) {
			com.stars.util.LogUtil.error("坐标格式异常 " + oldCoStr);
		}
		return co;
	}

	public void writeToBuff(NewByteBuffer buff) {
		buff.writeInt(x);
		buff.writeInt(y);
		buff.writeInt(z);
	}

	public String toStr(){
		StringBuilder builder = new StringBuilder();
		builder.append(x).append("+")
				.append(y).append("+")
				.append(z);
		return builder.toString();
	}

	public ThreeCoord copy(){
		try{
			return (ThreeCoord)super.clone();
		}catch (Exception e){
			LogUtil.error("三维坐标数据数据克隆失败", e);
		}
		return null;
	}


}
