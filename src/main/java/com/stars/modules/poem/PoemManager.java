package com.stars.modules.poem;

import com.stars.modules.poem.prodata.PoemVo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by gaopeidian on 2017/1/9.
 */
public class PoemManager {
	private static Map<Integer, PoemVo> poemVoMap = null;
	private static List<PoemVo> poemVoList = null;
	
	public static void setPoemVoMap(Map<Integer, PoemVo> map){
		poemVoMap = map;
		
		poemVoList = new ArrayList<PoemVo>();
		for (PoemVo vo : poemVoMap.values()) {
			poemVoList.add(vo);
		}
		
		Collections.sort(poemVoList);
    }
	
	public static Map<Integer, PoemVo> getPoemVoMapp(){
		return poemVoMap;
	}
	
	public static PoemVo getPoemVo(int poemId){
		return poemVoMap.get(poemId);
	}
	
	public static PoemVo getPoemVoByWorldId(int worldId){
		for (PoemVo vo : poemVoMap.values()) {
			if (vo.getWorldId() == worldId) {
				return vo;
			}
		}
		
		return null;
	}
	
	public static List<PoemVo> getPoemVoList(){
		return poemVoList;
	}
}
