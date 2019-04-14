package com.stars.modules.gm.gmhandler;

import com.stars.core.expr.ExprLexer;
import com.stars.core.expr.ExprParser;
import com.stars.core.expr.node.ExprNode;
import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.gm.GmHandler;
import com.stars.modules.role.RoleModule;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2017/3/25.
 */
public class PcGmHandler implements GmHandler {
    @Override
    public void handle(long roleId, Map<String, Module> moduleMap, String[] args) throws Exception {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            sb.append(args[i]);
            if (i != args.length-1) {
                sb.append(",");
            }
        }
        System.out.println(sb.toString());
        ExprNode node = new ExprParser(new ExprLexer(sb.toString())).parse();
        long ret = (long) node.eval(moduleMap);
        System.out.println(node.eval(moduleMap));
        ((RoleModule) moduleMap.get(MConst.Role)).warn("ret = " + ret);
    }
}
