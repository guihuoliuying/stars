package com.stars.modules.newequipment.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.core.player.PlayerUtil;
import com.stars.modules.MConst;
import com.stars.modules.newequipment.NewEquipmentModule;
import com.stars.modules.newequipment.NewEquipmentPacketSet;
import com.stars.modules.tool.ToolModule;
import com.stars.network.server.buffer.NewByteBuffer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wuyuxing on 2016/11/16.
 */
public class ServerNewEquipment extends PlayerPacket {

    public static final byte REQ_VIEW_MAIN = 0x00;      // 打开装备主界面
    public static final byte REQ_STRENGTH_EQUIP = 0x01; // 单次强化装备
    public static final byte REQ_AUTO_STRENGTH = 0x02;  // 一键自动强化装备
    public static final byte REQ_STAR_UP = 0x03;        // 升星
    public static final byte REQ_PUT_ON = 0x04;         // 穿上
    public static final byte REQ_TRANSFER = 0x05;       // 转移
    public static final byte REQ_WASH = 0x06;           // 洗练
    public static final byte REQ_RESOLVE = 0x07;        // 分解
    public static final byte REQ_RESOLVE_ALL = 0x08;    // 批量分解
    public static final byte REQ_RECOVER_WASH = 0x09;   // 还原洗练属性
    public static final byte REQ_WATCH_OTHER_EQUIP = 0x0a;//查看别人的装备
    public static final byte REQ_VIEW_MAIN_STRENGTH = 0x0b;//打开装备-强化
    public static final byte REQ_TOKEN_LEVEL_UP = 0x0c; //请求符文升级
    public static final byte REQ_TOKEN_WASH = 0x0d; //请求洗练符文装备
    public static final byte REQ_TOKEN_WASH_REPLACE = 0x0e; //请求替换符文洗练结果
    public static final byte REQ_TOKEN_EQUIPMENT_MELT = 0x0f; //请求熔炼符文装备
    public static final byte REQ_TOKEN_INHERIT = 0x10;//符文继承
    public static final byte REQ_TOKEN_TRANSFER_INHERIT = 0x11;//符文转移继承
    public static final byte REQ_OPEN_UPGRADE_UI = 0x12;//装备升级界面
    public static final byte REQ_UPGRADE_EQUIPMENT = 0x13;//请求装备升级
    public static final byte REQ_CAN_UPGRADE_EQUIPMENTS = 0x14;//请求可以升级装备列表


    private byte subtype;
    private byte type;
    private byte recover;
    private long toolId;
    private byte extAttrIndex;
    private int count;
    private byte useSaveItem;
    private byte resolveQuality;
    private long otherRoleId;
    private List<Byte> tokenIdsList = new ArrayList<>();
    //private byte isLockTokenSkill;
    private byte washSkillOrToken; //洗练符文还是符文技能 0--洗练符文技能 1--洗练符文
    private boolean includeProductData;

    @Override
    public void execPacket(Player player) {
        NewEquipmentModule equipmentModule = this.module(MConst.NewEquipment);
        ToolModule module = module(MConst.Tool);
        switch (subtype) {
            case REQ_VIEW_MAIN:         // 打开装备主界面
                equipmentModule.sendEquipOperateInfo();//下发装备操作相关信息
                break;
            case REQ_STRENGTH_EQUIP:    // 单次强化装备
                equipmentModule.requestStrengthEquip(type);
                break;
            case REQ_AUTO_STRENGTH:     // 一键自动强化装备
                equipmentModule.requestAutoStrengthEquip();
                break;
            case REQ_STAR_UP:           // 升星
                equipmentModule.requestUpStar(type, useSaveItem);
                break;
            case REQ_PUT_ON:            // 穿上
                equipmentModule.putOn(toolId);
                break;
            case REQ_TRANSFER:          // 转移
                equipmentModule.requestTransferEquip(type, toolId);
                break;
            case REQ_WASH:              // 洗练
                equipmentModule.requestWashEquip(type, toolId, extAttrIndex);
                break;
            case REQ_RESOLVE:           // 分解
                module.resolveTool(toolId, count);
                break;
            case REQ_RESOLVE_ALL:       // 批量分解
                module.resolveEquipByOneKey(resolveQuality);
                break;
            case REQ_RECOVER_WASH:      // 还原额外属性
                equipmentModule.recoverWashEquip(recover);
                break;
            case REQ_WATCH_OTHER_EQUIP:
                equipmentModule.watchOtherEquip(otherRoleId, type);
                break;
            case REQ_VIEW_MAIN_STRENGTH:
                ClientNewEquipment cne = new ClientNewEquipment(ClientNewEquipment.RESP_WITCH_EQUIP);
                cne.setWitchEquip(equipmentModule.canStrengthEquip());
                PlayerUtil.send(getRoleId(), cne);
                break;
            case REQ_TOKEN_LEVEL_UP:
                equipmentModule.reqTokenLevelUp(type, tokenIdsList);
                break;
            case REQ_TOKEN_WASH:
                equipmentModule.reqTokenEquipWash(type, tokenIdsList, washSkillOrToken);
                break;
            case REQ_TOKEN_WASH_REPLACE:
                equipmentModule.reqEnsureReplaceWashResult();
                break;
            case REQ_TOKEN_EQUIPMENT_MELT:
                equipmentModule.reqMeltTokenEquip(toolId);
                break;
            case REQ_TOKEN_INHERIT:
                equipmentModule.reqInheritTokenEquip(toolId);
                break;
            case REQ_TOKEN_TRANSFER_INHERIT:
                equipmentModule.reqTransferAndInheritTokenEquip(type, toolId);
                break;
            case REQ_OPEN_UPGRADE_UI: {
                equipmentModule.reqOpenUpgradeUI(includeProductData);
            }
            break;
            case REQ_UPGRADE_EQUIPMENT: {
                equipmentModule.reqUpgrade(type);
            }
            break;
            case REQ_CAN_UPGRADE_EQUIPMENTS: {
                equipmentModule.reqCanUpgradeList();
            }
            break;
            default:
                break;

        }
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        subtype = buff.readByte();
        switch (subtype) {
            case REQ_VIEW_MAIN:         // 打开装备主界面

                break;
            case REQ_STRENGTH_EQUIP:    // 单次强化装备
                this.type = buff.readByte();
                break;
            case REQ_AUTO_STRENGTH:     // 一键自动强化装备

                break;
            case REQ_STAR_UP:           // 升星
                this.type = buff.readByte();
                this.useSaveItem = buff.readByte();
                break;
            case REQ_PUT_ON:            // 穿上
                this.toolId = Long.parseLong(buff.readString(), 16);
                break;
            case REQ_TRANSFER:          // 转移
                this.type = buff.readByte();
                this.toolId = Long.parseLong(buff.readString(), 16);
                break;
            case REQ_WASH:              // 洗练
                this.type = buff.readByte();
                this.toolId = Long.parseLong(buff.readString(), 16);
                this.extAttrIndex = buff.readByte();
                break;
            case REQ_RESOLVE:           // 分解
                this.toolId = Long.parseLong(buff.readString(), 16);
                this.count = buff.readInt();
                break;
            case REQ_RESOLVE_ALL:       // 批量分解
                resolveQuality = buff.readByte();
                break;
            case REQ_RECOVER_WASH:      // 还原额外属性
                recover = buff.readByte();
                break;
            case REQ_WATCH_OTHER_EQUIP:
                otherRoleId = Long.parseLong(buff.readString());
                this.type = buff.readByte();
                break;
            case REQ_VIEW_MAIN_STRENGTH:
                break;
            case REQ_TOKEN_LEVEL_UP:
                this.type = buff.readByte(); //装备部位
                int size = buff.readInt();  //符文位数量
                for (int i = 0; i < size; i++) {
                    tokenIdsList.add(buff.readByte()); //符文位
                }
                break;
            case REQ_TOKEN_WASH:
                this.type = buff.readByte(); //装备部位
                int tokenHolesize = buff.readInt();  //符文位数量
                for (int i = 0; i < tokenHolesize; i++) {
                    tokenIdsList.add(buff.readByte()); //符文位
                }
                this.washSkillOrToken = buff.readByte(); //是否锁定符文技能
                break;
            case REQ_TOKEN_WASH_REPLACE:
                break;
            case REQ_TOKEN_EQUIPMENT_MELT:
                toolId = Long.parseLong(buff.readString(), 16);
                break;
            case REQ_TOKEN_INHERIT:
                toolId = Long.parseLong(buff.readString(), 16);
                break;
            case REQ_TOKEN_TRANSFER_INHERIT:
                type = buff.readByte();
                toolId = Long.parseLong(buff.readString(), 16);
                break;
            case REQ_OPEN_UPGRADE_UI: {
                includeProductData = buff.readInt() != 0;//是否需要产品数据：1为需要，0为不需要
            }
            break;
            case REQ_UPGRADE_EQUIPMENT: {
                type = buff.readByte();//装备位置
            }
            break;

            default:
                break;
        }
    }

    @Override
    public short getType() {
        return NewEquipmentPacketSet.S_NEW_EQUIPMENT;
    }
}
