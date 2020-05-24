package com.stars.modules.tool.productdata;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.role.RoleModule;
import com.stars.modules.tool.func.ToolFunc;
import com.stars.util.StringUtil;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by zhangjiahua on 2016/2/23.
 */
public class ItemVo {
    private int itemId;//道具的配置id
    private byte type;// 道具类型
    private String name;//名字
    private String desc;//描述
    private byte color;//品质颜色
    private String icon;//icon图片
    private int storage;//堆叠上限
    //    private String effect;// 使用后增加属性的属性名
//    private int effectValue;// 属性数值
    private String function;
    private ToolFunc toolFunc; // 暂时只考虑一种功能
    private byte funcType;
    private byte autoUse;
    private String getWay;
    private int[] sellPrice;//售卖价格
    private String model;// 掉落外观
    private Map<Byte, String> useCondition;
    private int[] buyPrice;//购买价格
    private String resolve;//物品分解得到的产物,格式为: itemid+数量, itemid+数量
    private Map<Integer, Integer> resolveMap;
    private String compound;// 合成所需
    private Integer rank;
    private int gradecoefftype;//等级系数


    /* 内存数据 */
    private Map<Integer, Integer> compoundNeeds = new HashMap<>();// 合成所需材料,<itemId, number>

    public Map<Integer, Integer> getCompoundNeeds() {
        return compoundNeeds;
    }

    public int getStorage() {
        return storage;
    }

    public void setStorage(int storage) {
        this.storage = storage;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public byte getColor() {
        return color;
    }

    public void setColor(byte color) {
        this.color = color;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public int getGradecoefftype() {
        return gradecoefftype;
    }

    public void setGradecoefftype(int gradecoefftype) {
        this.gradecoefftype = gradecoefftype;
    }

    public String getFunction() {
        return function;
    }

    public void setFunction(String function) {
        this.function = function;
    }

    public ToolFunc getToolFunc() {
        return toolFunc;
    }

    public void setToolFunc(ToolFunc toolFunc) {
        this.toolFunc = toolFunc;
    }

    public byte getAutoUse() {
        return autoUse;
    }

    public void setAutoUse(byte autoUse) {
        this.autoUse = autoUse;
    }

    public String getGetWay() {
        return getWay;
    }

    public void setGetWay(String getWay) {
        this.getWay = getWay;
    }

    public boolean isAutoUse() {
        return this.autoUse == (byte) 1;
    }

    public int[] getSellPrice() {
        return sellPrice;
    }

    public void setSellPrice(String sellPrice) {
        this.sellPrice = new int[2];
        if (sellPrice == null || sellPrice.equals("") || sellPrice.equals("0")) {
            return;
        }
        String str[] = sellPrice.split("[+]");
        this.sellPrice[0] = Integer.parseInt(str[0]);
        this.sellPrice[1] = Integer.parseInt(str[1]);
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Map<Byte, String> getUseCondition() {
        return useCondition;
    }

    public void setUseCondition(String condition) {
        if (condition == null || condition.equals("") || condition.equals("0")) {
            return;
        }
        this.useCondition = new HashMap<Byte, String>();
        String[] str1 = condition.split("\\|");
        for (String s1 : str1) {
            String[] str2 = s1.split("\\+");
            useCondition.put(Byte.parseByte(str2[0]), str2[1]);
        }
    }

    public int[] getBuyPrice() {
        return buyPrice;
    }

    public void setBuyPrice(String buyPrice) {
        if (buyPrice.equals("") || buyPrice.equals("0")) {
            return;
        }
        this.buyPrice = new int[2];
        String[] strs = buyPrice.split("[+]");
        this.buyPrice[0] = Integer.parseInt(strs[0]);
        this.buyPrice[1] = Integer.parseInt(strs[1]);
    }

    public String getResolve() {
        return resolve;
    }

    public void setResolve(String resolve) throws Exception {
        this.resolve = resolve;
        if (StringUtil.isEmpty(resolve) || resolve.equals("0")) return;
        this.resolveMap = StringUtil.toMap(resolve, Integer.class, Integer.class, '+', ',');
    }

    public Map<Integer, Integer> getResolveMap() {
        return resolveMap;
    }

    public byte getFuncType() {
        return funcType;
    }

    public void setFuncType(byte funcType) {
        this.funcType = funcType;
    }

    public String getCompound() {
        return compound;
    }

    public void setCompound(String compound) throws Exception {
        this.compound = compound;
        if (StringUtil.isEmpty(compound) || "0".equals(compound)) {
            return;
        }
        this.compoundNeeds = StringUtil.toMap(compound, Integer.class, Integer.class, '+', '|');
    }

    public boolean checkUseCondition(Map<String, Module> moduleMap) {
        if (StringUtil.isEmpty(useCondition)) return true;

        RoleModule rm = (RoleModule) moduleMap.get(MConst.Role);
        Set<Map.Entry<Byte, String>> set = useCondition.entrySet();
        for (Map.Entry<Byte, String> entry : set) {
            switch (entry.getKey()) {
                case 1://等级限制
                    if (rm.getLevel() < Integer.parseInt(entry.getValue())) {
                        return false;
                    }
                    break;
                case 3://开放某个系统
                    break;
                case 5://期限道具
                    Timestamp ts = Timestamp.valueOf(entry.getValue());
                    if (ts.after(new Timestamp(System.currentTimeMillis()))) {
                        return false;
                    }
                    break;
                default:
                    break;
            }
        }
        return true;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }
}
