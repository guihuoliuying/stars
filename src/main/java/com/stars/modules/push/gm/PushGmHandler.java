package com.stars.modules.push.gm;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.gm.GmHandler;
import com.stars.modules.push.PushModule;
import com.stars.modules.push.conditionparser.CondUtils;
import com.stars.modules.push.conditionparser.PushCondLexer;
import com.stars.modules.push.conditionparser.PushCondParser;
import com.stars.util.LogUtil;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2017/3/28.
 */
public class PushGmHandler implements GmHandler {

    @Override
    public void handle(long roleId, Map<String, Module> moduleMap, String[] args) throws Exception {
        PushModule pushModule = (PushModule) moduleMap.get(MConst.Push);
        switch (args[0]) {
            case "act":
                pushModule.activePush(Integer.parseInt(args[1]));
                break;
            case "ina":
                pushModule.inactivePush(Integer.parseInt(args[1]));
                break;
            case "baby": {
                boolean fuck = CondUtils.isTrue(new PushCondParser(new PushCondLexer("[babyfashion,id==2]==0 and babystage>1")).parse(), moduleMap);
                LogUtil.info("fuck:{}", fuck);
            }
            break;
            case "soul": {
                boolean soul = CondUtils.isTrue(new PushCondParser(new PushCondLexer("[soulgod,soulgodtype==1,soulgodlevel>1]==1")).parse(), moduleMap);
                System.err.println("精准推送表达式：元神：" + soul);
            }
            break;
            case "test": {
                StringBuilder sb = new StringBuilder();
                for (int index = 1; index <= args.length - 1; index++) {
                    if (index == 1) {
                        sb.append(args[index]);
                    } else {
                        sb.append(",").append(args[index]);
                    }
                }
                boolean soul = CondUtils.isTrue(new PushCondParser(new PushCondLexer(sb.toString())).parse(), moduleMap);
                pushModule.warn(soul + "");
            }
            break;
        }
    }

}
