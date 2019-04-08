package com.stars.services.family;

/**
 * Created by zhaowenshuo on 2016/9/30.
 */
public interface FamilyConst {

    int ACT_BTN_MASK_DISPLAY = 0x0001; // 显示/不显示
    int ACT_BTN_MASK_LIGHT = 0x0002; // 亮/灰
    int ACT_BTN_MASK_FLASH = 0x0004; // 闪/不闪
    int ACT_BTN_MASK_GLOW = 0x0008; // 流光/不流光
    int ACT_BTN_MASK_COUNTDOWN = 0x0010; // 倒计时/没有倒计时
    int ACT_BTN_MASK_ALL = 0x7FFF_FFFF; // 全部通过（高位）
    int ACT_BTN_MASK_NONE = 0x0000_0000; // 全部不通过

}
