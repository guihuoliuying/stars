package test;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;

public class testJson {
	
	public static void main(String[] args){
		Gson ga = new Gson();
		Bean b = new Bean();
		b.setTime("1");
		b.setId("2");
		String[]  str = new String[]{"3","4"};
		b.setArray(str);
		
		ArrayList<String> a = new ArrayList<String>();
		a.add("a");
		a.add("b");
		HashMap mapl = new HashMap<>();
		mapl.put("mapList", a);
		
		b.setMapList(mapl);
		HashMap<String, String>  tmap = new HashMap<String, String>();
		tmap.put("value", "1");
		tmap.put("value1", "2");
		b.setjMap(tmap);
		String s = ga.toJson(b);
		System.err.println(s);
		Bean c = new Bean();
		c = ga.fromJson(s, Bean.class);
		System.err.println("c="+c.getId());
		System.err.println("MapList="+c.getMapList().get("mapList").size());
	}
}
