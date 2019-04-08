package com.stars.modules.newequipment.prodata;

import java.util.ArrayList;
import java.util.List;

/**
 * 装备产出加成类，itemId=-1为无加成
 * Created by gaoepidian on 2017/2/28.
 */
public class EquipmentProduceAdd {
	public int itemId = -1;
	public int percent = 0;
	public List<Integer> targetIds = new ArrayList<Integer>();
}
