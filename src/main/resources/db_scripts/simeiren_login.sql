/*
Navicat MySQL Data Transfer

Source Server         : localhost_3306
Source Server Version : 50018
Source Host           : localhost:3306
Source Database       : simeiren_login

Target Server Type    : MYSQL
Target Server Version : 50018
File Encoding         : 65001

Date: 2019-04-24 18:11:31
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for `account`
-- ----------------------------
DROP TABLE IF EXISTS `account`;
CREATE TABLE `account` (
  `id` int(11) NOT NULL auto_increment,
  `uid` varchar(255) NOT NULL,
  `cpid` varchar(30) NOT NULL,
  `gameid` varchar(30) NOT NULL,
  `accountChannel` varchar(30) NOT NULL,
  `clientchannel` varchar(30) NOT NULL,
  `status` int(10) NOT NULL default '1',
  `roleCount` int(10) default NULL,
  `loginrole` varchar(30) default NULL,
  `loginserver` varchar(30) default NULL,
  `dateregister` datetime default '2010-09-15 01:00:00',
  `datelogin` datetime default NULL,
  `platform` varchar(20) default NULL,
  `memorytotal` varchar(20) default NULL,
  `memoryunused` varchar(20) default NULL,
  `nettpye` varchar(20) default NULL,
  `model` varchar(20) default NULL,
  `provider` varchar(20) default NULL,
  `sdk` varchar(20) default NULL,
  `sdkversion` varchar(20) default NULL,
  `resolution` varchar(20) default NULL,
  `cpu` varchar(20) default NULL,
  `cpumaxfreq` varchar(20) default NULL,
  `cpucores` varchar(20) default NULL,
  `hasSDcard` varchar(20) default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of account
-- ----------------------------

-- ----------------------------
-- Table structure for `account0`
-- ----------------------------
DROP TABLE IF EXISTS `account0`;
CREATE TABLE `account0` (
  `uid` varchar(255) NOT NULL COMMENT 'sdk里获得的uid,跟渠道号组合变唯一账号',
  `deviceid` varchar(64) NOT NULL COMMENT '设备id,暂时没用',
  `hcuid` varchar(64) NOT NULL COMMENT '幻城uid,暂时没有用',
  `accountChannel` varchar(30) NOT NULL COMMENT '账号渠道,注册渠道号',
  `username` varchar(200) default NULL COMMENT '用户名',
  `cpid` varchar(30) NOT NULL COMMENT 'cpid,sdk里获取的字段',
  `gameid` varchar(30) NOT NULL COMMENT '标示是那个游戏',
  `clientchannel` varchar(30) NOT NULL COMMENT '客户端渠道,登陆渠道',
  `ip` varchar(20) default NULL COMMENT 'ip地址',
  `status` varchar(20) default NULL COMMENT '状态信息',
  `roleCount` int(10) default NULL COMMENT '账号拥有的角色数',
  `loginrole` varchar(30) default NULL COMMENT '登陆的角色',
  `loginserver` varchar(30) default NULL COMMENT '登陆的区',
  `dateregister` datetime default '2010-09-15 01:00:00' COMMENT '注册时间',
  `datelogin` datetime default NULL COMMENT '最后登录 时间',
  `platform` varchar(20) default NULL COMMENT '平台',
  `memorytotal` varchar(20) default NULL COMMENT '内存总量',
  `memoryunused` varchar(20) default NULL COMMENT '已使用的内存',
  `nettpye` varchar(20) default NULL COMMENT '网络类型',
  `model` varchar(200) default NULL COMMENT '制式',
  `provider` varchar(20) default NULL COMMENT '运营提供商',
  `sdk` varchar(20) default NULL COMMENT 'sdk',
  `sdkversion` varchar(200) default NULL COMMENT 'sdk版本',
  `resolution` varchar(20) default NULL COMMENT '分辨率',
  `cpu` varchar(20) default NULL COMMENT 'cpu类型',
  `cpumaxfreq` varchar(20) default NULL COMMENT 'cpu频率',
  `cpucores` varchar(20) default NULL COMMENT 'cpu核心数',
  `hasSDcard` varchar(20) default NULL COMMENT '是否有SD卡',
  `releaseversion` varchar(20) default NULL,
  `IMEICode` varchar(20) default NULL COMMENT 'imei码',
  `versionCode` varchar(20) default NULL COMMENT '注册的版本号',
  `openkey` varchar(200) default '' COMMENT 'openkey',
  `servers` varchar(225) default NULL,
  PRIMARY KEY  (`uid`,`deviceid`,`accountChannel`,`clientchannel`),
  KEY `uid` USING BTREE (`uid`),
  KEY `accountChannel` USING BTREE (`accountChannel`),
  KEY `deviceid` USING BTREE (`deviceid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of account0
-- ----------------------------
INSERT INTO `account0` VALUES ('testuid', ' ', ' ', '10000', null, '1', '1026', '10001', '192.168.40.77', '1', '0', null, null, '2010-09-15 01:00:00', null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, '', null);

-- ----------------------------
-- Table structure for `account1`
-- ----------------------------
DROP TABLE IF EXISTS `account1`;
CREATE TABLE `account1` (
  `uid` varchar(255) NOT NULL,
  `deviceid` varchar(64) NOT NULL,
  `hcuid` varchar(64) NOT NULL,
  `accountChannel` varchar(30) NOT NULL,
  `username` varchar(200) default NULL,
  `cpid` varchar(30) NOT NULL,
  `gameid` varchar(30) NOT NULL,
  `clientchannel` varchar(30) NOT NULL,
  `ip` varchar(20) default NULL,
  `status` varchar(20) default NULL,
  `roleCount` int(10) default NULL,
  `loginrole` varchar(30) default NULL,
  `loginserver` varchar(30) default NULL,
  `dateregister` datetime default '2010-09-15 01:00:00',
  `datelogin` datetime default NULL,
  `platform` varchar(20) default NULL,
  `memorytotal` varchar(20) default NULL,
  `memoryunused` varchar(20) default NULL,
  `nettpye` varchar(20) default NULL,
  `model` varchar(200) default NULL,
  `provider` varchar(20) default NULL,
  `sdk` varchar(20) default NULL,
  `sdkversion` varchar(200) default NULL,
  `resolution` varchar(20) default NULL,
  `cpu` varchar(20) default NULL,
  `cpumaxfreq` varchar(20) default NULL,
  `cpucores` varchar(20) default NULL,
  `hasSDcard` varchar(20) default NULL,
  `releaseversion` varchar(20) default NULL,
  `IMEICode` varchar(20) default NULL,
  `versionCode` varchar(20) default NULL,
  `openkey` varchar(200) default '',
  `servers` varchar(225) default NULL,
  PRIMARY KEY  (`uid`,`deviceid`,`accountChannel`,`clientchannel`),
  KEY `uid` USING BTREE (`uid`),
  KEY `accountChannel` USING BTREE (`accountChannel`),
  KEY `deviceid` USING BTREE (`deviceid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of account1
-- ----------------------------

-- ----------------------------
-- Table structure for `account2`
-- ----------------------------
DROP TABLE IF EXISTS `account2`;
CREATE TABLE `account2` (
  `uid` varchar(255) NOT NULL COMMENT 'sdk里获得的uid,跟渠道号组合变唯一账号',
  `deviceid` varchar(64) NOT NULL,
  `hcuid` varchar(64) NOT NULL,
  `accountChannel` varchar(30) NOT NULL,
  `username` varchar(200) default NULL,
  `cpid` varchar(30) NOT NULL,
  `gameid` varchar(30) NOT NULL,
  `clientchannel` varchar(30) NOT NULL,
  `ip` varchar(20) default NULL,
  `status` varchar(20) default NULL,
  `roleCount` int(10) default NULL,
  `loginrole` varchar(30) default NULL,
  `loginserver` varchar(30) default NULL,
  `dateregister` datetime default '2010-09-15 01:00:00',
  `datelogin` datetime default NULL,
  `platform` varchar(20) default NULL,
  `memorytotal` varchar(20) default NULL,
  `memoryunused` varchar(20) default NULL,
  `nettpye` varchar(20) default NULL,
  `model` varchar(200) default NULL,
  `provider` varchar(20) default NULL,
  `sdk` varchar(20) default NULL,
  `sdkversion` varchar(200) default NULL,
  `resolution` varchar(20) default NULL,
  `cpu` varchar(20) default NULL,
  `cpumaxfreq` varchar(20) default NULL,
  `cpucores` varchar(20) default NULL,
  `hasSDcard` varchar(20) default NULL,
  `releaseversion` varchar(20) default NULL,
  `IMEICode` varchar(20) default NULL,
  `versionCode` varchar(20) default NULL,
  `openkey` varchar(200) default '',
  `servers` varchar(225) default NULL,
  PRIMARY KEY  (`uid`,`deviceid`,`accountChannel`,`clientchannel`),
  KEY `uid` USING BTREE (`uid`),
  KEY `accountChannel` USING BTREE (`accountChannel`),
  KEY `deviceid` USING BTREE (`deviceid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of account2
-- ----------------------------

-- ----------------------------
-- Table structure for `account3`
-- ----------------------------
DROP TABLE IF EXISTS `account3`;
CREATE TABLE `account3` (
  `uid` varchar(255) NOT NULL COMMENT 'sdk里获得的uid,跟渠道号组合变唯一账号',
  `deviceid` varchar(64) NOT NULL,
  `hcuid` varchar(64) NOT NULL,
  `accountChannel` varchar(30) NOT NULL,
  `username` varchar(200) default NULL,
  `cpid` varchar(30) NOT NULL,
  `gameid` varchar(30) NOT NULL,
  `clientchannel` varchar(30) NOT NULL,
  `ip` varchar(20) default NULL,
  `status` varchar(20) default NULL,
  `roleCount` int(10) default NULL,
  `loginrole` varchar(30) default NULL,
  `loginserver` varchar(30) default NULL,
  `dateregister` datetime default '2010-09-15 01:00:00',
  `datelogin` datetime default NULL,
  `platform` varchar(20) default NULL,
  `memorytotal` varchar(20) default NULL,
  `memoryunused` varchar(20) default NULL,
  `nettpye` varchar(20) default NULL,
  `model` varchar(200) default NULL,
  `provider` varchar(20) default NULL,
  `sdk` varchar(20) default NULL,
  `sdkversion` varchar(200) default NULL,
  `resolution` varchar(20) default NULL,
  `cpu` varchar(20) default NULL,
  `cpumaxfreq` varchar(20) default NULL,
  `cpucores` varchar(20) default NULL,
  `hasSDcard` varchar(20) default NULL,
  `releaseversion` varchar(20) default NULL,
  `IMEICode` varchar(20) default NULL,
  `versionCode` varchar(20) default NULL,
  `openkey` varchar(200) default '',
  `servers` varchar(225) default NULL,
  PRIMARY KEY  (`uid`,`deviceid`,`accountChannel`,`clientchannel`),
  KEY `uid` USING BTREE (`uid`),
  KEY `accountChannel` USING BTREE (`accountChannel`),
  KEY `deviceid` USING BTREE (`deviceid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of account3
-- ----------------------------

-- ----------------------------
-- Table structure for `account4`
-- ----------------------------
DROP TABLE IF EXISTS `account4`;
CREATE TABLE `account4` (
  `uid` varchar(255) NOT NULL COMMENT 'sdk里获得的uid,跟渠道号组合变唯一账号',
  `deviceid` varchar(64) NOT NULL,
  `hcuid` varchar(64) NOT NULL,
  `accountChannel` varchar(30) NOT NULL,
  `username` varchar(200) default NULL,
  `cpid` varchar(30) NOT NULL,
  `gameid` varchar(30) NOT NULL,
  `clientchannel` varchar(30) NOT NULL,
  `ip` varchar(20) default NULL,
  `status` varchar(20) default NULL,
  `roleCount` int(10) default NULL,
  `loginrole` varchar(30) default NULL,
  `loginserver` varchar(30) default NULL,
  `dateregister` datetime default '2010-09-15 01:00:00',
  `datelogin` datetime default NULL,
  `platform` varchar(20) default NULL,
  `memorytotal` varchar(20) default NULL,
  `memoryunused` varchar(20) default NULL,
  `nettpye` varchar(20) default NULL,
  `model` varchar(200) default NULL,
  `provider` varchar(20) default NULL,
  `sdk` varchar(20) default NULL,
  `sdkversion` varchar(200) default NULL,
  `resolution` varchar(20) default NULL,
  `cpu` varchar(20) default NULL,
  `cpumaxfreq` varchar(20) default NULL,
  `cpucores` varchar(20) default NULL,
  `hasSDcard` varchar(20) default NULL,
  `releaseversion` varchar(20) default NULL,
  `IMEICode` varchar(20) default NULL,
  `versionCode` varchar(20) default NULL,
  `openkey` varchar(200) default '',
  `servers` varchar(225) default NULL,
  PRIMARY KEY  (`uid`,`deviceid`,`accountChannel`,`clientchannel`),
  KEY `uid` USING BTREE (`uid`),
  KEY `accountChannel` USING BTREE (`accountChannel`),
  KEY `deviceid` USING BTREE (`deviceid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of account4
-- ----------------------------

-- ----------------------------
-- Table structure for `account5`
-- ----------------------------
DROP TABLE IF EXISTS `account5`;
CREATE TABLE `account5` (
  `uid` varchar(255) NOT NULL COMMENT 'sdk里获得的uid,跟渠道号组合变唯一账号',
  `deviceid` varchar(64) NOT NULL,
  `hcuid` varchar(64) NOT NULL,
  `accountChannel` varchar(30) NOT NULL,
  `username` varchar(200) default NULL,
  `cpid` varchar(30) NOT NULL,
  `gameid` varchar(30) NOT NULL,
  `clientchannel` varchar(30) NOT NULL,
  `ip` varchar(20) default NULL,
  `status` varchar(20) default NULL,
  `roleCount` int(10) default NULL,
  `loginrole` varchar(30) default NULL,
  `loginserver` varchar(30) default NULL,
  `dateregister` datetime default '2010-09-15 01:00:00',
  `datelogin` datetime default NULL,
  `platform` varchar(20) default NULL,
  `memorytotal` varchar(20) default NULL,
  `memoryunused` varchar(20) default NULL,
  `nettpye` varchar(20) default NULL,
  `model` varchar(200) default NULL,
  `provider` varchar(20) default NULL,
  `sdk` varchar(20) default NULL,
  `sdkversion` varchar(200) default NULL,
  `resolution` varchar(20) default NULL,
  `cpu` varchar(20) default NULL,
  `cpumaxfreq` varchar(20) default NULL,
  `cpucores` varchar(20) default NULL,
  `hasSDcard` varchar(20) default NULL,
  `releaseversion` varchar(20) default NULL,
  `IMEICode` varchar(20) default NULL,
  `versionCode` varchar(20) default NULL,
  `openkey` varchar(200) default '',
  `servers` varchar(225) default NULL,
  PRIMARY KEY  (`uid`,`deviceid`,`accountChannel`,`clientchannel`),
  KEY `uid` USING BTREE (`uid`),
  KEY `accountChannel` USING BTREE (`accountChannel`),
  KEY `deviceid` USING BTREE (`deviceid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of account5
-- ----------------------------

-- ----------------------------
-- Table structure for `account6`
-- ----------------------------
DROP TABLE IF EXISTS `account6`;
CREATE TABLE `account6` (
  `uid` varchar(255) NOT NULL COMMENT 'sdk里获得的uid,跟渠道号组合变唯一账号',
  `deviceid` varchar(64) NOT NULL,
  `hcuid` varchar(64) NOT NULL,
  `accountChannel` varchar(30) NOT NULL,
  `username` varchar(200) default NULL,
  `cpid` varchar(30) NOT NULL,
  `gameid` varchar(30) NOT NULL,
  `clientchannel` varchar(30) NOT NULL,
  `ip` varchar(20) default NULL,
  `status` varchar(20) default NULL,
  `roleCount` int(10) default NULL,
  `loginrole` varchar(30) default NULL,
  `loginserver` varchar(30) default NULL,
  `dateregister` datetime default '2010-09-15 01:00:00',
  `datelogin` datetime default NULL,
  `platform` varchar(20) default NULL,
  `memorytotal` varchar(20) default NULL,
  `memoryunused` varchar(20) default NULL,
  `nettpye` varchar(20) default NULL,
  `model` varchar(200) default NULL,
  `provider` varchar(20) default NULL,
  `sdk` varchar(20) default NULL,
  `sdkversion` varchar(200) default NULL,
  `resolution` varchar(20) default NULL,
  `cpu` varchar(20) default NULL,
  `cpumaxfreq` varchar(20) default NULL,
  `cpucores` varchar(20) default NULL,
  `hasSDcard` varchar(20) default NULL,
  `releaseversion` varchar(20) default NULL,
  `IMEICode` varchar(20) default NULL,
  `versionCode` varchar(20) default NULL,
  `openkey` varchar(200) default '',
  `servers` varchar(225) default NULL,
  PRIMARY KEY  (`uid`,`deviceid`,`accountChannel`,`clientchannel`),
  KEY `uid` USING BTREE (`uid`),
  KEY `accountChannel` USING BTREE (`accountChannel`),
  KEY `deviceid` USING BTREE (`deviceid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of account6
-- ----------------------------

-- ----------------------------
-- Table structure for `account7`
-- ----------------------------
DROP TABLE IF EXISTS `account7`;
CREATE TABLE `account7` (
  `uid` varchar(255) NOT NULL COMMENT 'sdk里获得的uid,跟渠道号组合变唯一账号',
  `deviceid` varchar(64) NOT NULL,
  `hcuid` varchar(64) NOT NULL,
  `accountChannel` varchar(30) NOT NULL,
  `username` varchar(200) default NULL,
  `cpid` varchar(30) NOT NULL,
  `gameid` varchar(30) NOT NULL,
  `clientchannel` varchar(30) NOT NULL,
  `ip` varchar(20) default NULL,
  `status` varchar(20) default NULL,
  `roleCount` int(10) default NULL,
  `loginrole` varchar(30) default NULL,
  `loginserver` varchar(30) default NULL,
  `dateregister` datetime default '2010-09-15 01:00:00',
  `datelogin` datetime default NULL,
  `platform` varchar(20) default NULL,
  `memorytotal` varchar(20) default NULL,
  `memoryunused` varchar(20) default NULL,
  `nettpye` varchar(20) default NULL,
  `model` varchar(200) default NULL,
  `provider` varchar(20) default NULL,
  `sdk` varchar(20) default NULL,
  `sdkversion` varchar(200) default NULL,
  `resolution` varchar(20) default NULL,
  `cpu` varchar(20) default NULL,
  `cpumaxfreq` varchar(20) default NULL,
  `cpucores` varchar(20) default NULL,
  `hasSDcard` varchar(20) default NULL,
  `releaseversion` varchar(20) default NULL,
  `IMEICode` varchar(20) default NULL,
  `versionCode` varchar(20) default NULL,
  `openkey` varchar(200) default '',
  `servers` varchar(225) default NULL,
  PRIMARY KEY  (`uid`,`deviceid`,`accountChannel`,`clientchannel`),
  KEY `uid` USING BTREE (`uid`),
  KEY `accountChannel` USING BTREE (`accountChannel`),
  KEY `deviceid` USING BTREE (`deviceid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of account7
-- ----------------------------

-- ----------------------------
-- Table structure for `account8`
-- ----------------------------
DROP TABLE IF EXISTS `account8`;
CREATE TABLE `account8` (
  `uid` varchar(255) NOT NULL COMMENT 'sdk里获得的uid,跟渠道号组合变唯一账号',
  `deviceid` varchar(64) NOT NULL,
  `hcuid` varchar(64) NOT NULL,
  `accountChannel` varchar(30) NOT NULL,
  `username` varchar(200) default NULL,
  `cpid` varchar(30) NOT NULL,
  `gameid` varchar(30) NOT NULL,
  `clientchannel` varchar(30) NOT NULL,
  `ip` varchar(20) default NULL,
  `status` varchar(20) default NULL,
  `roleCount` int(10) default NULL,
  `loginrole` varchar(30) default NULL,
  `loginserver` varchar(30) default NULL,
  `dateregister` datetime default '2010-09-15 01:00:00',
  `datelogin` datetime default NULL,
  `platform` varchar(20) default NULL,
  `memorytotal` varchar(20) default NULL,
  `memoryunused` varchar(20) default NULL,
  `nettpye` varchar(20) default NULL,
  `model` varchar(200) default NULL,
  `provider` varchar(20) default NULL,
  `sdk` varchar(20) default NULL,
  `sdkversion` varchar(200) default NULL,
  `resolution` varchar(20) default NULL,
  `cpu` varchar(20) default NULL,
  `cpumaxfreq` varchar(20) default NULL,
  `cpucores` varchar(20) default NULL,
  `hasSDcard` varchar(20) default NULL,
  `releaseversion` varchar(20) default NULL,
  `IMEICode` varchar(20) default NULL,
  `versionCode` varchar(20) default NULL,
  `openkey` varchar(200) default '',
  `servers` varchar(225) default NULL,
  PRIMARY KEY  (`uid`,`deviceid`,`accountChannel`,`clientchannel`),
  KEY `uid` USING BTREE (`uid`),
  KEY `accountChannel` USING BTREE (`accountChannel`),
  KEY `deviceid` USING BTREE (`deviceid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of account8
-- ----------------------------

-- ----------------------------
-- Table structure for `account9`
-- ----------------------------
DROP TABLE IF EXISTS `account9`;
CREATE TABLE `account9` (
  `uid` varchar(255) NOT NULL COMMENT 'sdk里获得的uid,跟渠道号组合变唯一账号',
  `deviceid` varchar(64) NOT NULL,
  `hcuid` varchar(64) NOT NULL,
  `accountChannel` varchar(30) NOT NULL,
  `username` varchar(200) default NULL,
  `cpid` varchar(30) NOT NULL,
  `gameid` varchar(30) NOT NULL,
  `clientchannel` varchar(30) NOT NULL,
  `ip` varchar(20) default NULL,
  `status` varchar(20) default NULL,
  `roleCount` int(10) default NULL,
  `loginrole` varchar(30) default NULL,
  `loginserver` varchar(30) default NULL,
  `dateregister` datetime default '2010-09-15 01:00:00',
  `datelogin` datetime default NULL,
  `platform` varchar(20) default NULL,
  `memorytotal` varchar(20) default NULL,
  `memoryunused` varchar(20) default NULL,
  `nettpye` varchar(20) default NULL,
  `model` varchar(200) default NULL,
  `provider` varchar(20) default NULL,
  `sdk` varchar(20) default NULL,
  `sdkversion` varchar(200) default NULL,
  `resolution` varchar(20) default NULL,
  `cpu` varchar(20) default NULL,
  `cpumaxfreq` varchar(20) default NULL,
  `cpucores` varchar(20) default NULL,
  `hasSDcard` varchar(20) default NULL,
  `releaseversion` varchar(20) default NULL,
  `IMEICode` varchar(20) default NULL,
  `versionCode` varchar(20) default NULL,
  `openkey` varchar(200) default '',
  `servers` varchar(225) default NULL,
  PRIMARY KEY  (`uid`,`deviceid`,`accountChannel`,`clientchannel`),
  KEY `uid` USING BTREE (`uid`),
  KEY `accountChannel` USING BTREE (`accountChannel`),
  KEY `deviceid` USING BTREE (`deviceid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of account9
-- ----------------------------

-- ----------------------------
-- Table structure for `account_ip`
-- ----------------------------
DROP TABLE IF EXISTS `account_ip`;
CREATE TABLE `account_ip` (
  `account` varchar(255) NOT NULL,
  `ip` varchar(20) NOT NULL,
  PRIMARY KEY  (`account`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of account_ip
-- ----------------------------

-- ----------------------------
-- Table structure for `account_last_server`
-- ----------------------------
DROP TABLE IF EXISTS `account_last_server`;
CREATE TABLE `account_last_server` (
  `account` varchar(100) NOT NULL default '' COMMENT '账号',
  `serverid` varchar(30) NOT NULL default '-1' COMMENT '最后一次登录的服务器ID',
  `last_login_time` timestamp NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP COMMENT '最后一次登录时间',
  PRIMARY KEY  (`account`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of account_last_server
-- ----------------------------
INSERT INTO `account_last_server` VALUES ('user23', '0', '2017-05-13 15:42:03');

-- ----------------------------
-- Table structure for `auth_user_groups`
-- ----------------------------
DROP TABLE IF EXISTS `auth_user_groups`;
CREATE TABLE `auth_user_groups` (
  `id` int(11) NOT NULL auto_increment,
  `user_id` int(11) NOT NULL,
  `group_id` int(11) NOT NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `user_id` USING BTREE (`user_id`,`group_id`),
  KEY `auth_user_groups_6340c63c` USING BTREE (`user_id`),
  KEY `auth_user_groups_5f412f9a` USING BTREE (`group_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of auth_user_groups
-- ----------------------------

-- ----------------------------
-- Table structure for `auth_user_user_permissions`
-- ----------------------------
DROP TABLE IF EXISTS `auth_user_user_permissions`;
CREATE TABLE `auth_user_user_permissions` (
  `id` int(11) NOT NULL auto_increment,
  `user_id` int(11) NOT NULL,
  `permission_id` int(11) NOT NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `user_id` USING BTREE (`user_id`,`permission_id`),
  KEY `auth_user_user_permissions_6340c63c` USING BTREE (`user_id`),
  KEY `auth_user_user_permissions_83d7f98b` USING BTREE (`permission_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of auth_user_user_permissions
-- ----------------------------

-- ----------------------------
-- Table structure for `blockage`
-- ----------------------------
DROP TABLE IF EXISTS `blockage`;
CREATE TABLE `blockage` (
  `blockageaccount` varchar(255) NOT NULL default '' COMMENT '封号账号',
  `overtime` varchar(30) NOT NULL default '' COMMENT '解封时间',
  `reason` varchar(10) NOT NULL default '' COMMENT '封号原因',
  PRIMARY KEY  (`blockageaccount`),
  KEY `usrid` USING BTREE (`blockageaccount`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of blockage
-- ----------------------------

-- ----------------------------
-- Table structure for `channelinfo`
-- ----------------------------
DROP TABLE IF EXISTS `channelinfo`;
CREATE TABLE `channelinfo` (
  `channelid` int(11) NOT NULL default '0' COMMENT '主渠道号',
  `subchannelid` int(11) NOT NULL default '0' COMMENT '子渠道号',
  PRIMARY KEY  (`channelid`,`subchannelid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of channelinfo
-- ----------------------------

-- ----------------------------
-- Table structure for `common`
-- ----------------------------
DROP TABLE IF EXISTS `common`;
CREATE TABLE `common` (
  `key` varchar(255) character set gbk NOT NULL COMMENT '键',
  `value` longtext character set gbk NOT NULL COMMENT '值',
  PRIMARY KEY  (`key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of common
-- ----------------------------
INSERT INTO `common` VALUES ('serverchannel', 'zyy');

-- ----------------------------
-- Table structure for `configstring`
-- ----------------------------
DROP TABLE IF EXISTS `configstring`;
CREATE TABLE `configstring` (
  `key` varchar(64) collate utf8_bin NOT NULL,
  `value` varchar(512) collate utf8_bin default NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

-- ----------------------------
-- Records of configstring
-- ----------------------------
INSERT INTO `configstring` VALUES ('maintenceinfo', '预计1秒后开服,啊哈哈哈哈哈哈!');

-- ----------------------------
-- Table structure for `connect_infos`
-- ----------------------------
DROP TABLE IF EXISTS `connect_infos`;
CREATE TABLE `connect_infos` (
  `server_ip` varchar(15) default NULL,
  `port` int(6) default NULL,
  `auth_key` varchar(40) default NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of connect_infos
-- ----------------------------

-- ----------------------------
-- Table structure for `db_instance`
-- ----------------------------
DROP TABLE IF EXISTS `db_instance`;
CREATE TABLE `db_instance` (
  `id` int(11) NOT NULL auto_increment,
  `master_ip` char(15) NOT NULL,
  `master_port` int(11) NOT NULL,
  `slave_ip` char(15) NOT NULL,
  `slave_port` int(11) NOT NULL,
  `data_path` varchar(200) NOT NULL,
  `contain_dbs` varchar(300) default NULL,
  `connect_num` int(11) NOT NULL,
  `delay_ms` int(11) NOT NULL,
  `data_size` varchar(50) NOT NULL,
  `bak_btime` datetime default NULL,
  `bak_etime` datetime default NULL,
  `hotbak_server` varchar(200) NOT NULL,
  `hotbak_path` varchar(200) NOT NULL,
  `coldbak_path` varchar(200) NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of db_instance
-- ----------------------------

-- ----------------------------
-- Table structure for `django_admin_log`
-- ----------------------------
DROP TABLE IF EXISTS `django_admin_log`;
CREATE TABLE `django_admin_log` (
  `id` int(11) NOT NULL auto_increment,
  `action_time` datetime NOT NULL,
  `user_id` int(11) NOT NULL,
  `content_type_id` int(11) default NULL,
  `object_id` longtext,
  `object_repr` varchar(200) NOT NULL,
  `action_flag` smallint(5) unsigned NOT NULL,
  `change_message` longtext NOT NULL,
  PRIMARY KEY  (`id`),
  KEY `django_admin_log_6340c63c` USING BTREE (`user_id`),
  KEY `django_admin_log_37ef4eb4` USING BTREE (`content_type_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of django_admin_log
-- ----------------------------

-- ----------------------------
-- Table structure for `django_cas_ng_proxygrantingticket`
-- ----------------------------
DROP TABLE IF EXISTS `django_cas_ng_proxygrantingticket`;
CREATE TABLE `django_cas_ng_proxygrantingticket` (
  `id` int(11) NOT NULL auto_increment,
  `session_key` varchar(255) default NULL,
  `user_id` int(11) default NULL,
  `pgtiou` varchar(255) default NULL,
  `pgt` varchar(255) default NULL,
  `date` datetime NOT NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `session_key` USING BTREE (`session_key`,`user_id`),
  KEY `django_cas_ng_proxygrantingticket_e8701ad4` USING BTREE (`user_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of django_cas_ng_proxygrantingticket
-- ----------------------------

-- ----------------------------
-- Table structure for `django_cas_ng_sessionticket`
-- ----------------------------
DROP TABLE IF EXISTS `django_cas_ng_sessionticket`;
CREATE TABLE `django_cas_ng_sessionticket` (
  `id` int(11) NOT NULL auto_increment,
  `session_key` varchar(255) NOT NULL,
  `ticket` varchar(255) NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of django_cas_ng_sessionticket
-- ----------------------------

-- ----------------------------
-- Table structure for `django_cas_pgtiou`
-- ----------------------------
DROP TABLE IF EXISTS `django_cas_pgtiou`;
CREATE TABLE `django_cas_pgtiou` (
  `id` int(11) NOT NULL auto_increment,
  `pgtIou` varchar(255) NOT NULL,
  `tgt` varchar(255) NOT NULL,
  `timestamp` datetime NOT NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `pgtIou` USING BTREE (`pgtIou`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of django_cas_pgtiou
-- ----------------------------

-- ----------------------------
-- Table structure for `django_cas_session_service_ticket`
-- ----------------------------
DROP TABLE IF EXISTS `django_cas_session_service_ticket`;
CREATE TABLE `django_cas_session_service_ticket` (
  `service_ticket` varchar(255) NOT NULL,
  `session_key` varchar(40) NOT NULL,
  PRIMARY KEY  (`service_ticket`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of django_cas_session_service_ticket
-- ----------------------------

-- ----------------------------
-- Table structure for `django_cas_tgt`
-- ----------------------------
DROP TABLE IF EXISTS `django_cas_tgt`;
CREATE TABLE `django_cas_tgt` (
  `id` int(11) NOT NULL auto_increment,
  `username` varchar(255) NOT NULL,
  `tgt` varchar(255) NOT NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `username` USING BTREE (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of django_cas_tgt
-- ----------------------------

-- ----------------------------
-- Table structure for `django_content_type`
-- ----------------------------
DROP TABLE IF EXISTS `django_content_type`;
CREATE TABLE `django_content_type` (
  `id` int(11) NOT NULL auto_increment,
  `name` varchar(100) NOT NULL,
  `app_label` varchar(100) NOT NULL,
  `model` varchar(100) NOT NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `app_label` USING BTREE (`app_label`,`model`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of django_content_type
-- ----------------------------

-- ----------------------------
-- Table structure for `django_migrations`
-- ----------------------------
DROP TABLE IF EXISTS `django_migrations`;
CREATE TABLE `django_migrations` (
  `id` int(11) NOT NULL auto_increment,
  `app` varchar(255) NOT NULL,
  `name` varchar(255) NOT NULL,
  `applied` datetime NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of django_migrations
-- ----------------------------

-- ----------------------------
-- Table structure for `django_session`
-- ----------------------------
DROP TABLE IF EXISTS `django_session`;
CREATE TABLE `django_session` (
  `session_key` varchar(40) NOT NULL,
  `session_data` longtext NOT NULL,
  `expire_date` datetime NOT NULL,
  PRIMARY KEY  (`session_key`),
  KEY `django_session_b7b81f0c` USING BTREE (`expire_date`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of django_session
-- ----------------------------

-- ----------------------------
-- Table structure for `django_site`
-- ----------------------------
DROP TABLE IF EXISTS `django_site`;
CREATE TABLE `django_site` (
  `id` int(11) NOT NULL auto_increment,
  `domain` varchar(100) NOT NULL,
  `name` varchar(50) NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of django_site
-- ----------------------------

-- ----------------------------
-- Table structure for `excel_log`
-- ----------------------------
DROP TABLE IF EXISTS `excel_log`;
CREATE TABLE `excel_log` (
  `id` int(11) NOT NULL auto_increment,
  `excel_file_name` varchar(100) default NULL,
  `excel_file_md5` varchar(100) default NULL,
  `excel_file_upload_time` datetime default NULL,
  `excel_file_upload_user` varchar(50) default NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of excel_log
-- ----------------------------

-- ----------------------------
-- Table structure for `gameboard`
-- ----------------------------
DROP TABLE IF EXISTS `gameboard`;
CREATE TABLE `gameboard` (
  `boardid` int(11) NOT NULL auto_increment COMMENT '公告板id',
  `title` varchar(255) NOT NULL COMMENT '公告标题',
  `text` text NOT NULL,
  `date` varchar(255) NOT NULL COMMENT '公告时间',
  `label` tinyint(255) NOT NULL default '0' COMMENT '公告标签类型, 1=最新, 2=热门.配0代表无标签',
  `serverdate` varchar(255) NOT NULL default '0' COMMENT '服开时间',
  `plateform` varchar(255) NOT NULL default '0' COMMENT '平台',
  PRIMARY KEY  (`boardid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of gameboard
-- ----------------------------
INSERT INTO `gameboard` VALUES ('1', '1', '1', '2017-4-1&2017-5-20', '0', '0', '0');

-- ----------------------------
-- Table structure for `greenaccount`
-- ----------------------------
DROP TABLE IF EXISTS `greenaccount`;
CREATE TABLE `greenaccount` (
  `account` varchar(255) NOT NULL,
  `name` varchar(225) default NULL
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of greenaccount
-- ----------------------------

-- ----------------------------
-- Table structure for `greenmac`
-- ----------------------------
DROP TABLE IF EXISTS `greenmac`;
CREATE TABLE `greenmac` (
  `id` int(11) NOT NULL COMMENT 'id',
  `mac` varchar(25) NOT NULL COMMENT '有绿色账号权限的mac地址',
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of greenmac
-- ----------------------------

-- ----------------------------
-- Table structure for `inetaccount`
-- ----------------------------
DROP TABLE IF EXISTS `inetaccount`;
CREATE TABLE `inetaccount` (
  `account` varchar(255) NOT NULL default '' COMMENT '账号',
  `sid` varchar(127) NOT NULL default '' COMMENT '密码',
  `time` timestamp NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP COMMENT '时间',
  PRIMARY KEY  (`account`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of inetaccount
-- ----------------------------
INSERT INTO `inetaccount` VALUES ('1001', 'aaaddd', '2016-12-14 17:47:55');

-- ----------------------------
-- Table structure for `log`
-- ----------------------------
DROP TABLE IF EXISTS `log`;
CREATE TABLE `log` (
  `id` int(11) NOT NULL auto_increment,
  `log_user` varchar(30) default NULL,
  `log_date` datetime default NULL,
  `log_type` varchar(50) default NULL,
  `log_ip` char(15) default NULL,
  `log_status` varchar(50) default NULL,
  `log_pmodel` varchar(30) default NULL,
  `log_detail` longtext,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of log
-- ----------------------------

-- ----------------------------
-- Table structure for `notcreateaccount`
-- ----------------------------
DROP TABLE IF EXISTS `notcreateaccount`;
CREATE TABLE `notcreateaccount` (
  `ip` varchar(30) NOT NULL COMMENT '禁止注册的ip',
  PRIMARY KEY  (`ip`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of notcreateaccount
-- ----------------------------

-- ----------------------------
-- Table structure for `page`
-- ----------------------------
DROP TABLE IF EXISTS `page`;
CREATE TABLE `page` (
  `page_id` int(11) NOT NULL auto_increment COMMENT '页id',
  `page_name` varchar(30) default NULL COMMENT '页标题',
  `page_servers` varchar(200) default NULL COMMENT '页的服务器列表',
  PRIMARY KEY  (`page_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of page
-- ----------------------------
INSERT INTO `page` VALUES ('1', '推荐服', '7463');
INSERT INTO `page` VALUES ('2', '思美人1-2服', '7381');

-- ----------------------------
-- Table structure for `pay_server`
-- ----------------------------
DROP TABLE IF EXISTS `pay_server`;
CREATE TABLE `pay_server` (
  `server_id` varchar(30) NOT NULL,
  `server_tip` varchar(30) default NULL,
  `server_name` varchar(30) default NULL,
  `server_ip` char(15) default NULL,
  `server_port` int(11) default NULL,
  `server_proxy` int(11) default NULL,
  `server_sort` int(11) default NULL,
  `server_localip` char(15) default NULL,
  `server_dbport` int(11) default NULL,
  `server_status` varchar(30) default NULL,
  `server_lgstatus` varchar(30) default NULL,
  `server_path` varchar(40) default NULL,
  `server_path_launcher` varchar(40) default NULL,
  `server_to_launcher` int(11) default NULL,
  `launcher_to_server` int(11) default NULL,
  `server_mem` varchar(40) default NULL,
  `launcher_mem` varchar(40) default NULL,
  `server_http` int(11) NOT NULL,
  `db_port` int(11) default NULL,
  `db_ip` char(15) default NULL,
  `db_name` varchar(300) default NULL,
  `server_hqinfo` varchar(200) default NULL,
  PRIMARY KEY  (`server_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of pay_server
-- ----------------------------

-- ----------------------------
-- Table structure for `pserverinfo`
-- ----------------------------
DROP TABLE IF EXISTS `pserverinfo`;
CREATE TABLE `pserverinfo` (
  `id` varchar(5) NOT NULL COMMENT '公共服serverid',
  `type` varchar(64) NOT NULL COMMENT '公共服类型',
  `name` varchar(64) NOT NULL COMMENT '公共服名字',
  `ip` varchar(64) NOT NULL COMMENT '公共服ip',
  `port` varchar(5) NOT NULL COMMENT '公共服端口',
  `path` varchar(64) default NULL COMMENT '公共服所在目录',
  `pserver_mem` varchar(40) default NULL COMMENT '公共服内存'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of pserverinfo
-- ----------------------------
INSERT INTO `pserverinfo` VALUES ('7082', 'loottreasure', '夺宝服', '127.0.0.1', '1777', null, null);
INSERT INTO `pserverinfo` VALUES ('7042', 'multi', '斗神殿', '127.0.0.1', '1888', null, null);
INSERT INTO `pserverinfo` VALUES ('7061', 'rmchat', '聊天服', '127.0.0.1', '1666', null, null);
INSERT INTO `pserverinfo` VALUES ('820', 'fightmanager', '战斗管理服', '127.0.0.1', '7080', null, null);
INSERT INTO `pserverinfo` VALUES ('821', 'fightmanager1', '战斗管理服1', '127.0.0.1', '7180', null, null);
INSERT INTO `pserverinfo` VALUES ('901', 'fightServer', '战斗服', '127.0.0.1', '9081', null, null);
INSERT INTO `pserverinfo` VALUES ('333', 'payserver', '支付服', '127.0.0.1', '8771', null, null);
INSERT INTO `pserverinfo` VALUES ('9002', 'daily5v5', '日常5v5', '127.0.0.1', '8100', null, null);
INSERT INTO `pserverinfo` VALUES ('9001', 'skyrank', '天梯服', '127.0.0.1', '1444', null, null);
INSERT INTO `pserverinfo` VALUES ('9003', 'familywar', '跨服家族战', '127.0.0.1', '1999', null, null);
INSERT INTO `pserverinfo` VALUES ('9999', 'camp', '阵营', '127.0.0.1', '1222', null, null);

-- ----------------------------
-- Table structure for `rolecount`
-- ----------------------------
DROP TABLE IF EXISTS `rolecount`;
CREATE TABLE `rolecount` (
  `id` bigint(16) NOT NULL auto_increment COMMENT 'id',
  `serverid` int(8) NOT NULL COMMENT '区号',
  `count` int(8) NOT NULL COMMENT '此区拥有的角色数',
  `account` varchar(255) NOT NULL COMMENT '账号',
  PRIMARY KEY  (`id`),
  KEY `count` USING BTREE (`count`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of rolecount
-- ----------------------------

-- ----------------------------
-- Table structure for `server`
-- ----------------------------
DROP TABLE IF EXISTS `server`;
CREATE TABLE `server` (
  `server_id` varchar(30) NOT NULL default '-1' COMMENT 'serverid,刚开区时的serverid',
  `server_tip` varchar(30) NOT NULL default '-1' COMMENT '真正的serverid,合区后从服跟主服指向的同一个serverid',
  `server_name` varchar(30) default NULL COMMENT '区名字',
  `server_ip` char(15) default NULL COMMENT '区所在的外网ip',
  `server_port` int(11) default NULL COMMENT '区所在机器端口',
  `server_proxy` int(11) default NULL COMMENT '代理ip',
  `server_sort` int(11) default NULL,
  `server_localip` char(15) default NULL COMMENT '区所在机器的内网ip',
  `server_dbport` int(11) default NULL COMMENT '区所用数据库的端口',
  `server_status` varchar(30) default NULL COMMENT '服务状态',
  `server_lgstatus` varchar(30) default NULL,
  `server_path` varchar(40) default NULL COMMENT '服务部署的目录',
  `server_path_launcher` varchar(40) default NULL COMMENT '服务转发器部署的目录',
  `server_to_launcher` int(11) default NULL COMMENT '服务到转发器的端口',
  `launcher_to_server` int(11) default NULL COMMENT '转发器到服务的端口',
  `server_mem` varchar(40) default NULL COMMENT '服务启动内存',
  `launcher_mem` varchar(40) default NULL COMMENT '转发器启动内存',
  `server_http` int(11) NOT NULL COMMENT '服务的http端口',
  `db_port` int(11) default NULL COMMENT '登陆服数据库端口',
  `db_ip` char(15) default NULL COMMENT '登陆服数据库ip',
  `db_name` varchar(300) default NULL COMMENT '数据库名',
  `server_hqinfo` text COMMENT '合区信息',
  `slave_db_ip` char(39) default NULL COMMENT '从库ip',
  `slave_db_name` varchar(100) default NULL COMMENT '从库名字',
  `slave_db_port` int(11) default NULL COMMENT '从库端口',
  PRIMARY KEY  (`server_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of server
-- ----------------------------
INSERT INTO `server` VALUES ('2002', '2001', '马可222', '192.168.30.102', '9005', null, '1', '192.168.30.102', '3306', '1', '1', null, null, null, '8091', null, null, '9001', '3306', '127.0.0.1', 'user1', '', null, null, null);
INSERT INTO `server` VALUES ('2001', '2002', '思美人11', '192.168.30.102', '9005', null, '1', '192.168.30.102', '3306', '1', '1', null, null, null, '8091', null, null, '9001', '3306', '127.0.0.1', 'user2', '', null, null, null);

-- ----------------------------
-- Table structure for `server1`
-- ----------------------------
DROP TABLE IF EXISTS `server1`;
CREATE TABLE `server1` (
  `server_id` int(10) NOT NULL auto_increment,
  `server_name` varchar(30) NOT NULL,
  `server_ip` char(15) NOT NULL,
  `server_port` int(11) NOT NULL,
  `server_proxy` int(11) default '0',
  `server_sort` int(11) default '3',
  `server_localip` char(15) NOT NULL,
  `server_dbport` int(11) default '1',
  `server_status` varchar(30) default '1',
  `server_lgstatus` varchar(30) default '22',
  `server_path` varchar(40) default '/data/gamedata/center/server',
  `server_http` int(11) NOT NULL,
  `db_port` int(11) default '3306',
  `db_ip` char(15) default NULL,
  `db_name` varchar(300) default NULL,
  `server_hqinfo` varchar(200) default NULL,
  PRIMARY KEY  (`server_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of server1
-- ----------------------------

-- ----------------------------
-- Table structure for `server_openconfig`
-- ----------------------------
DROP TABLE IF EXISTS `server_openconfig`;
CREATE TABLE `server_openconfig` (
  `minserverid` int(10) NOT NULL default '0',
  `recomm_count` int(10) NOT NULL default '0',
  `open_server_limit_perday` int(10) NOT NULL default '0',
  `recomm_regcount_limit` int(10) NOT NULL default '0',
  `recomm_online_limit` int(10) NOT NULL default '0',
  `recomm_regcount_limit_not_openserver` int(10) NOT NULL default '0',
  `page_name` varchar(16) NOT NULL default '',
  `forbid_open_server_times` varchar(128) NOT NULL default '',
  `loadtime` varchar(32) NOT NULL default ''
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of server_openconfig
-- ----------------------------

-- ----------------------------
-- Table structure for `server_prepare`
-- ----------------------------
DROP TABLE IF EXISTS `server_prepare`;
CREATE TABLE `server_prepare` (
  `server_id` int(10) NOT NULL,
  `isused` tinyint(2) NOT NULL default '0',
  `opendate` varchar(32) NOT NULL default '',
  `ispay` tinyint(2) NOT NULL default '0',
  PRIMARY KEY  (`server_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of server_prepare
-- ----------------------------

-- ----------------------------
-- Table structure for `slavedb_instance`
-- ----------------------------
DROP TABLE IF EXISTS `slavedb_instance`;
CREATE TABLE `slavedb_instance` (
  `id` int(11) NOT NULL auto_increment,
  `slavedb_ip` char(15) NOT NULL,
  `slavedb_user` varchar(20) NOT NULL,
  `slavedb_script` varchar(200) NOT NULL,
  `slavedb_remark` varchar(100) NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of slavedb_instance
-- ----------------------------

-- ----------------------------
-- Table structure for `specialaccount`
-- ----------------------------
DROP TABLE IF EXISTS `specialaccount`;
CREATE TABLE `specialaccount` (
  `account` varchar(255) NOT NULL COMMENT '殊特账号',
  PRIMARY KEY  (`account`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of specialaccount
-- ----------------------------
INSERT INTO `specialaccount` VALUES ('a');
INSERT INTO `specialaccount` VALUES ('b');

-- ----------------------------
-- Table structure for `sup_server`
-- ----------------------------
DROP TABLE IF EXISTS `sup_server`;
CREATE TABLE `sup_server` (
  `id` int(11) NOT NULL auto_increment,
  `sup_name` varchar(30) NOT NULL,
  `sup_jarname` varchar(30) NOT NULL,
  `sup_localip` char(15) NOT NULL,
  `sup_port` int(11) default NULL,
  `sup_path` varchar(40) NOT NULL,
  `sup_mem` varchar(30) NOT NULL,
  `sup_status` varchar(30) NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of sup_server
-- ----------------------------

-- ----------------------------
-- Table structure for `task`
-- ----------------------------
DROP TABLE IF EXISTS `task`;
CREATE TABLE `task` (
  `task_id` int(11) NOT NULL auto_increment,
  `task_name` varchar(50) default NULL,
  `task_cmd` varchar(2000) default NULL,
  `task_status` int(11) default NULL,
  `task_tip` varchar(2000) default NULL,
  `task_user` varchar(100) default NULL,
  `task_stime` datetime default NULL,
  `task_info` varchar(50) default NULL,
  `task_type` varchar(50) default NULL,
  PRIMARY KEY  (`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of task
-- ----------------------------

-- ----------------------------
-- Table structure for `threadpoolconf`
-- ----------------------------
DROP TABLE IF EXISTS `threadpoolconf`;
CREATE TABLE `threadpoolconf` (
  `code` varchar(30) NOT NULL default '001' COMMENT '线程池标识符',
  `active` int(6) NOT NULL default '1000' COMMENT '线程池活跃线程数量限制',
  `current` int(6) NOT NULL default '2000' COMMENT '线程池当前线程限制',
  `switch` int(6) NOT NULL default '0' COMMENT '制限起效开关，默认为0不起效，1为起效',
  PRIMARY KEY  (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of threadpoolconf
-- ----------------------------

-- ----------------------------
-- Table structure for `upload_files`
-- ----------------------------
DROP TABLE IF EXISTS `upload_files`;
CREATE TABLE `upload_files` (
  `procedure_id` int(11) NOT NULL,
  `file_name` varchar(500) default NULL,
  `full_path` varchar(500) default NULL,
  `upload_time` int(11) default NULL,
  `md5_result` varchar(64) default NULL,
  `file_size` int(11) default NULL,
  `upload_user` varchar(64) default NULL,
  PRIMARY KEY  (`procedure_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of upload_files
-- ----------------------------

-- ----------------------------
-- Table structure for `user_privilege`
-- ----------------------------
DROP TABLE IF EXISTS `user_privilege`;
CREATE TABLE `user_privilege` (
  `id` int(11) NOT NULL auto_increment,
  `privilege_name` varchar(30) NOT NULL,
  `privilege_id` int(11) NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of user_privilege
-- ----------------------------

-- ----------------------------
-- Table structure for `user_totalpri`
-- ----------------------------
DROP TABLE IF EXISTS `user_totalpri`;
CREATE TABLE `user_totalpri` (
  `totalpri_id` int(11) NOT NULL,
  `totalpri_category` varchar(30) NOT NULL,
  `totalpri_url` varchar(200) NOT NULL,
  `totalpri_parent` int(11) NOT NULL,
  PRIMARY KEY  (`totalpri_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of user_totalpri
-- ----------------------------

-- ----------------------------
-- Table structure for `user_user`
-- ----------------------------
DROP TABLE IF EXISTS `user_user`;
CREATE TABLE `user_user` (
  `user_name` varchar(30) NOT NULL,
  `user_password` varchar(200) default NULL,
  `user_type` varchar(5) NOT NULL,
  PRIMARY KEY  (`user_name`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of user_user
-- ----------------------------

-- ----------------------------
-- Table structure for `verifyquestionatr`
-- ----------------------------
DROP TABLE IF EXISTS `verifyquestionatr`;
CREATE TABLE `verifyquestionatr` (
  `id` int(11) NOT NULL default '0' COMMENT 'id',
  `question` varchar(255) NOT NULL default '' COMMENT '验证码问题',
  `answer` varchar(255) NOT NULL default '' COMMENT '答案',
  `eganswer` varchar(255) NOT NULL default '',
  `dayofweek` smallint(3) NOT NULL COMMENT '时间',
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of verifyquestionatr
-- ----------------------------

-- ----------------------------
-- Table structure for `webhd_init_path_cmd`
-- ----------------------------
DROP TABLE IF EXISTS `webhd_init_path_cmd`;
CREATE TABLE `webhd_init_path_cmd` (
  `id` int(11) NOT NULL auto_increment,
  `proj_name` varchar(160) NOT NULL,
  `file_name` varchar(160) NOT NULL,
  `local_dir` varchar(160) NOT NULL,
  `remote_dir` varchar(160) NOT NULL,
  `local_cmd` varchar(1024) NOT NULL,
  `remote_cmd` varchar(1024) NOT NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `proj_name` USING BTREE (`proj_name`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of webhd_init_path_cmd
-- ----------------------------

-- ----------------------------
-- Table structure for `webhd_server_infos`
-- ----------------------------
DROP TABLE IF EXISTS `webhd_server_infos`;
CREATE TABLE `webhd_server_infos` (
  `id` int(11) NOT NULL auto_increment,
  `server_model` varchar(60) NOT NULL,
  `server_idc` varchar(60) NOT NULL,
  `server_type` varchar(60) NOT NULL,
  `server_cpu` int(11) NOT NULL,
  `server_mem` int(11) NOT NULL,
  `server_disk` varchar(30) NOT NULL,
  `server_volume` int(10) unsigned NOT NULL,
  `server_raid` varchar(30) NOT NULL,
  `server_pip` char(15) NOT NULL,
  `server_ip` char(15) NOT NULL,
  `server_net` varchar(30) NOT NULL,
  `server_remarks` varchar(80) NOT NULL,
  `server_buytime` date NOT NULL,
  `server_usetime` date NOT NULL,
  `server_lastupdate` datetime NOT NULL,
  `server_status` varchar(10) NOT NULL,
  `server_hefu` tinyint(1) NOT NULL,
  `server_active` tinyint(1) NOT NULL,
  `server_check` tinyint(1) NOT NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `server_pip` USING BTREE (`server_pip`),
  UNIQUE KEY `server_ip` USING BTREE (`server_ip`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of webhd_server_infos
-- ----------------------------

-- ----------------------------
-- Table structure for `webser_crongame`
-- ----------------------------
DROP TABLE IF EXISTS `webser_crongame`;
CREATE TABLE `webser_crongame` (
  `id` int(11) NOT NULL auto_increment,
  `server_id` int(11) NOT NULL,
  `server_name` varchar(50) NOT NULL,
  `server_time` datetime NOT NULL,
  `user` varchar(50) NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of webser_crongame
-- ----------------------------

-- ----------------------------
-- Table structure for `webshow_config`
-- ----------------------------
DROP TABLE IF EXISTS `webshow_config`;
CREATE TABLE `webshow_config` (
  `id` int(11) NOT NULL,
  `base_app_num` int(11) NOT NULL,
  `base_db_num` int(11) NOT NULL,
  `alert_app_num` int(11) NOT NULL,
  `alert_db_num` int(11) NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of webshow_config
-- ----------------------------
