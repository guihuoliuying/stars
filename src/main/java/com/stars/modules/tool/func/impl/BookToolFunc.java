package com.stars.modules.tool.func.impl;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.book.BookModule;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.tool.func.ToolFunc;
import com.stars.modules.tool.func.ToolFuncResult;
import com.stars.modules.tool.productdata.ItemVo;
import com.stars.util.StringUtil;

import java.util.Map;

/**
 * Created by zhoujin on 2017/4/14.
 */
public class BookToolFunc extends ToolFunc {
    private int bookId = -1;

    public BookToolFunc(ItemVo itemVo) {
        super(itemVo);
    }

    @Override
    public void parseData(String function) {
        if (StringUtil.isEmpty(function)) return;
        String[] args = function.split("\\|");
        
        if (args.length >= 2) {
            bookId = Integer.parseInt(args[1]);
		}
        
        if (args.length >= 3) {
        	parseNotice(args[2]);
		}

    }

    @Override
    public ToolFuncResult check(Map<String, Module> moduleMap, int count, Object... args) {
    	if (count <= 0) {
            return new ToolFuncResult(false, new ClientText("道具数量为零"));
        }
    	
    	ToolFuncResult tr = super.useCondition(moduleMap, args);
        if (tr == null) {
            tr = new ToolFuncResult(true, null);
        }
        if (canAutoUse(moduleMap,count) == 0) {
            tr.setSuccess(false);
            tr.setMessage(new ClientText("典籍已收集满"));
        }
            
        return tr;
    }

    @Override
    public int canAutoUse(Map<String, Module> moduleMap, int count) {
        if (count <= 0) return 0;
        BookModule bookModule = (BookModule) moduleMap.get(MConst.Book);
        int num = bookModule.canAddBookNum(bookId,count);
        return num;
    }

    @Override
    public Map<Integer,Integer> use(Map<String, Module> moduleMap, int count, Object... args) {
        if (count <= 0) return null;
        BookModule bookModule = (BookModule) moduleMap.get(MConst.Book);
        bookModule.addBookNum(bookId,count);
        return null;
    }
}
