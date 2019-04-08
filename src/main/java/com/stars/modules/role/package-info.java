/**
 * Created by zhaowenshuo on 2016/1/14.
 */
package com.stars.modules.role;

/*
 * Role模块协议号使用范围0x0040 - 0x004F
 * 
 * 当前已使用协议号:
 * 	short C_ROLE = 0x0040; 下发玩家基础数据
 * 
 * role表DDL:
 * CREATE TABLE `role` (
 * 	`roleid` bigint(20) NOT NULL DEFAULT '0',
 * 	`name` varchar(255) DEFAULT NULL,
 * 	`level` int(11) NOT NULL DEFAULT '0',
 * 	`vip` int(11) NOT NULL DEFAULT '0',
 * 	`people` bigint(15) NOT NULL DEFAULT '0',
 * 	`food` bigint(15) NOT NULL DEFAULT '0',
 * 	`gold` bigint(15) NOT NULL DEFAULT '0',
 * 	`rowversion` bigint(20) NOT NULL DEFAULT '0',
 * 	PRIMARY KEY (`roleid`)
 * ) ENGINE=InnoDB DEFAULT CHARSET=utf8;
 * 
 * 
 */

