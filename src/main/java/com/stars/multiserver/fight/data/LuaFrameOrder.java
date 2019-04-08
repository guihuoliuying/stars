package com.stars.multiserver.fight.data;

import java.util.HashMap;
import java.util.List;

/**
 * Created by zhouxiaogang on 2016/11/18.
 * 服务端LUA每帧输出的指令
 */

public class LuaFrameOrder{
    private byte[] order;//同步至客户端的指令集
    private HashMap<String, byte[]> specificorder;//给特定玩家的指令 key:玩家ID value:指令集
    private List<MultiOrder> multiorder;//给多个玩家的相同指令
    private List<String> log;//日志

    public class MultiOrder {
        private List<String> idlist;//一组玩家ID
        private byte[] pack;//指令数据

        public byte[] getPack() {
            return pack;
        }

        public void setPack(byte[] pack) {
            this.pack = pack;
        }

        public List<String> getIdlist() {
            return idlist;
        }

        public void setIdlist(List<String> idlist) {
            this.idlist = idlist;
        }
    }

    public byte[] getOrder() {
        return order;
    }

    public void setOrder(byte[] order) {
        this.order = order;
    }

    public HashMap<String, byte[]> getSpecificorder() {
        return specificorder;
    }

    public void setSpecificorder(HashMap<String, byte[]> specificorder) {
        this.specificorder = specificorder;
    }

    public List<MultiOrder> getMultiorder() {
        return multiorder;
    }

    public void setMultiorder(List<MultiOrder> multiorder) {
        this.multiorder = multiorder;
    }

    public List<String> getLog() {
        return log;
    }

    public void setLog(List<String> log) {
        this.log = log;
    }
}

/**
public class LuaFrameOrder {
    private byte[] order;
    private HashMap<String, byte[]> specificorder;
    private List<MultiOrder> multiorder;

    public class MultiOrder {
        private List<String> idlist;//一组玩家ID
        private byte[] pack;//指令数据

        public byte[] getPack() {
            return pack;
        }

        public void setPack(byte[] pack) {
            this.pack = pack;
        }

        public List<String> getIdlist() {
            return idlist;
        }

        public void setIdlist(List<String> idlist) {
            this.idlist = idlist;
        }
    }

    public void setOrder(byte[] order){this.order = order;}
    public void setSpecificOrder(HashMap<String, byte[]> specificOrder){this.specificorder = specificOrder;}
    public void setMultiOrder(List<MultiOrder>  multiOrder){this.multiorder = multiOrder;}
    public byte[] getOrder(){return this.order;}
    public HashMap<String, byte[]> getSpecificorder(){return this.specificorder;}
    public List<MultiOrder> getMultiorder(){return this.multiorder;}

    public static byte[] converLuaOrderToBytes(String order){
        if(order == null || order.equals("")){
            return null;
        }
        char[] chars = order.toCharArray();
        int i,lstrLength = 0;
        char c;
        for (i = lstrLength = chars.length; --i>0;){
            if((c=chars[i]) >= 0x80)
                lstrLength += (c >= 0x800) ? 2:1;
        }
        byte[] bytes = new byte[lstrLength];
        int cIndex = 0;

        for(i = 0; cIndex < lstrLength; ++cIndex) {
            if((c = chars[cIndex]) < 128) {
                bytes[i++] = (byte)c;
            } else if(c < 2048) {
                bytes[i++] = (byte)(192 | c >> 6 & 31);
                bytes[i++] = (byte)(128 | c & 63);
            } else {
                bytes[i++] = (byte)(224 | c >> 12 & 15);
                bytes[i++] = (byte)(128 | c >> 6 & 63);
                bytes[i++] = (byte)(128 | c & 63);
            }
        }
        return bytes;
    }

    public void decode(LuaObject lo){
        LuaState L = lo.getLuaState();
        L.getTable(1);
        L.pushNil();
        String key = null;
        while(L.next(1) != 0){
            key = L.toString(-2);
            if(key == "order"){
                this.order = converLuaOrderToBytes(L.toString(-1));
            }
            else if(key == "specificorder"){
                L.getTable(-1);
                L.pushNil();
                specificorder = new HashMap<>();
                while(L.next(1) != 0) {
                    specificorder.put(L.toString(-1), converLuaOrderToBytes(L.toString(-2)));
                    L.pop(1);
                }
                L.pop(1);
            }
            else if(key == "multiorder"){
                L.getTable(-1);
                L.pushNil();
                multiorder = new ArrayList<>();
                MultiOrder mo = null;
                List<String> idList = null;
                while(L.next(1) != 0){
                    mo = new MultiOrder();
                    while(L.next(-1) != 0){
                        key = L.toString(-2);
                        if(key == "idlist"){
                            idList = new ArrayList<>();
                            while(L.next(1) != 0){
                                idList.add(L.toString(-2));
                                L.pop(1);
                            }
                            mo.setIdlist(idList);
                        }
                        else if(key == "pack"){
                            mo.setPack(converLuaOrderToBytes(L.toString(-2)));
                        }
                    }
                }
            }
        }
    }
}
*/