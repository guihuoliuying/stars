package com.stars.modules.name;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.core.db.DBUtil;
import com.stars.modules.data.DataManager;
import com.stars.util.StringUtil;
import com.stars.util._HashMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NameModuleFactory extends AbstractModuleFactory<NameModule> {
	public NameModuleFactory(){
		super(new NamePacketSet());
	}
	@Override
    public NameModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> map) {
        return new NameModule(id, self, eventDispatcher, map);
    }
	@Override
    public void init() throws Exception {

    }
	
    @Override
    public void registerListener(EventDispatcher eventDispatcher, Module module) {

    }
    
    @Override
    public void loadProductData() throws Exception {
		List<com.stars.util._HashMap> ls = DBUtil.queryList(DBUtil.DB_PRODUCT, com.stars.util._HashMap.class, "select * from randname");
    	List<String> firstName = new ArrayList<String>();
    	List<String> secondName = new ArrayList<String>();
    	List<String> thirdName = new ArrayList<String>();
    	for (_HashMap map:ls) {
			firstName.add(map.getString("firstname"));
    		secondName.add(map.getString("secondname"));
    		thirdName.add(map.getString("thirdname"));
		}
    	NameManager.firstName = firstName;
    	NameManager.secondName = secondName;
    	NameManager.thirdName = thirdName;

		NameManager.maxRenameTime = DataManager.getCommConfig("changename_changetimes", 0);
		NameManager.renameCd = DataManager.getCommConfig("changename_changecd", 0);
		String cost = DataManager.getCommConfig("changename_reqchange");
		Map<Integer, Integer> costItemMap = StringUtil.toMap(cost, Integer.class, Integer.class, '+', '|');
		NameManager.costItemMap = costItemMap;
    }
}
