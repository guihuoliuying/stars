package test;

import java.util.ArrayList;
import java.util.HashMap;

public class Bean {
	String time;
	String id;
	String[] array = null;
	HashMap<String, String> jMap = new HashMap<>(); 
	
	HashMap<String,ArrayList<String>> mapList = new HashMap<>();
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String[] getArray() {
		return array;
	}
	public void setArray(String[] array) {
		this.array = array;
	}
	public HashMap<String, String> getjMap() {
		return jMap;
	}
	public void setjMap(HashMap<String, String> jMap) {
		this.jMap = jMap;
	}
	public HashMap<String, ArrayList<String>> getMapList() {
		return mapList;
	}
	public void setMapList(HashMap<String, ArrayList<String>> mapList) {
		this.mapList = mapList;
	}
}
