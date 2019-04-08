package com.stars.multiserver.fight;

import org.keplerproject.luajava.JavaFunction;
import org.keplerproject.luajava.LuaException;
import org.keplerproject.luajava.LuaObject;
import org.keplerproject.luajava.LuaState;

/**
 * Created by zhaowenshuo on 2017/3/8.
 */
public class LuaRequireFunc extends JavaFunction {

    public LuaRequireFunc(LuaState l) {
        super(l);
    }

    @Override
    public int execute() throws LuaException {
        // 参数
        String filename = getParam(2).getString();
//        System.out.println("LuaRequireFunc: " + filename);
        //
//        long s = System.nanoTime();
        L.pushObjectValue(luaLoadString(filename).call(new Object[] {}));
//        long e = System.nanoTime();
//        long delta = e - s;
//        if (delta > 10000000) {
//        LogUtil.info("require|script:{}|elapsed:{}ns", filename, delta);
//        }
        return 1;
    }


    private LuaObject luaLoadString(String filename) throws LuaException {
        return (LuaObject) L.getLuaObject("loadstring").call(
                new Object[] { LuaScripts.get(filename) });
    }

    private LuaObject luaAssert(LuaObject chuck) throws LuaException {
        return (LuaObject) L.getLuaObject("assert").call(new Object[] { chuck });
    }

}
