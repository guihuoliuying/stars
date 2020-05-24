/*
Navicat MySQL Data Transfer

Source Server         : localhost_3306
Source Server Version : 50018
Source Host           : localhost:3306
Source Database       : simeiren_common

Target Server Type    : MYSQL
Target Server Version : 50018
File Encoding         : 65001

Date: 2019-04-24 18:12:07
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for `accountchargeback`
-- ----------------------------
DROP TABLE IF EXISTS `accountchargeback`;
CREATE TABLE `accountchargeback` (
  `account` varchar(255) NOT NULL,
  `vipexp` int(11) NOT NULL default '0' COMMENT 'vip经验',
  `yb` int(11) NOT NULL default '0' COMMENT '充值元宝数',
  `monthcard` int(2) NOT NULL default '0' COMMENT '是否使用月卡',
  PRIMARY KEY  (`account`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='账号充值返利记录';

-- ----------------------------
-- Records of accountchargeback
-- ----------------------------

-- ----------------------------
-- Table structure for `allservercamp`
-- ----------------------------
DROP TABLE IF EXISTS `allservercamp`;
CREATE TABLE `allservercamp` (
  `camptype` int(11) NOT NULL,
  `prosperousnum` bigint(11) NOT NULL COMMENT '繁荣度',
  `level` int(11) NOT NULL COMMENT '阵营等级',
  `rolenum` int(11) NOT NULL COMMENT '玩家数量',
  PRIMARY KEY  (`camptype`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of allservercamp
-- ----------------------------
INSERT INTO `allservercamp` VALUES ('1', '11313172', '4', '56');
INSERT INTO `allservercamp` VALUES ('2', '11251138', '4', '73');

-- ----------------------------
-- Table structure for `campcityplayer`
-- ----------------------------
DROP TABLE IF EXISTS `campcityplayer`;
CREATE TABLE `campcityplayer` (
  `cityid` int(11) NOT NULL COMMENT '城池id',
  `playerinfo` text COMMENT '城池玩家数据',
  `totalnum` int(11) NOT NULL default '0' COMMENT '城市人数',
  PRIMARY KEY  (`cityid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of campcityplayer
-- ----------------------------
INSERT INTO `campcityplayer` VALUES ('1', '4195218+1|4194711+1|4194709+1|4195807+1|4194363+1|4194723+1|4194722+1|4195510+1|4195779+1|4195211+1|4194792+1|4195210+1|4194835+1|4195208+1|4195421+1|4195594+1|4195989+1|4194799+1|4195991+1|4194561+1', '21');
INSERT INTO `campcityplayer` VALUES ('2', '4194570+1', '1');
INSERT INTO `campcityplayer` VALUES ('9', '4194496+1|4194501+1|4194718+1|4194716+1|4194717+1|4194506+1|4194914+1|4194715+1|4194712+1|4194331+1|4194520+1|4194522+1|4194459+1|4195960+1|4195699+1|4194344+1|4194532+1|4194536+1|4195559+1|4195830+1|4194472+1|4194721+1|4195206+1|4194549+1|4195687+1|4195209+1|4195988+1|4195213+1|4195990+1|4195137+1|4195212+1', '31');

-- ----------------------------
-- Table structure for `dissolvefamily`
-- ----------------------------
DROP TABLE IF EXISTS `dissolvefamily`;
CREATE TABLE `dissolvefamily` (
  `familyid` bigint(20) NOT NULL,
  PRIMARY KEY  (`familyid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of dissolvefamily
-- ----------------------------
INSERT INTO `dissolvefamily` VALUES ('4194348');

-- ----------------------------
-- Table structure for `familywarfixture`
-- ----------------------------
DROP TABLE IF EXISTS `familywarfixture`;
CREATE TABLE `familywarfixture` (
  `wartype` int(20) NOT NULL COMMENT '比赛类型1：本服赛，2：跨服海选，3跨服决赛',
  `serverid` int(20) NOT NULL COMMENT '该家族所在的serverid',
  `fixturefamily` varchar(255) NOT NULL COMMENT '家族对阵表',
  PRIMARY KEY  (`wartype`,`serverid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of familywarfixture
-- ----------------------------
INSERT INTO `familywarfixture` VALUES ('1', '1', '0=0|1=0|2=0|3=0|4=0|5=0|6=4194306|7=4194315|8=4194305|9=4194319|10=4194308|11=4194314|12=4194337|13=4194333|14=0|15=0');

-- ----------------------------
-- Table structure for `familywarremotefamily`
-- ----------------------------
DROP TABLE IF EXISTS `familywarremotefamily`;
CREATE TABLE `familywarremotefamily` (
  `familyid` bigint(20) NOT NULL,
  `serverid` int(11) NOT NULL,
  `battletype` int(11) default NULL,
  `rank` int(11) default NULL,
  `index` varchar(255) default ''
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of familywarremotefamily
-- ----------------------------
INSERT INTO `familywarremotefamily` VALUES ('4194305', '1001', '1', '1', '6+2+0');
INSERT INTO `familywarremotefamily` VALUES ('4194306', '1001', '1', '3', '12+5+15');
INSERT INTO `familywarremotefamily` VALUES ('4194307', '1001', '1', '4', '8+3+14');
INSERT INTO `familywarremotefamily` VALUES ('4194308', '1001', '1', '2', '10+4+1');
INSERT INTO `familywarremotefamily` VALUES ('4194309', '1001', '16', '16', '');
INSERT INTO `familywarremotefamily` VALUES ('4194310', '1001', '16', '16', '');
INSERT INTO `familywarremotefamily` VALUES ('4194311', '1001', '1', '8', '13');
INSERT INTO `familywarremotefamily` VALUES ('4194312', '1001', '16', '16', '');
INSERT INTO `familywarremotefamily` VALUES ('4194313', '1001', '16', '16', '');
INSERT INTO `familywarremotefamily` VALUES ('4194314', '1001', '16', '16', '');
INSERT INTO `familywarremotefamily` VALUES ('4194315', '1001', '16', '16', '');
INSERT INTO `familywarremotefamily` VALUES ('4194316', '1001', '1', '8', '9');
INSERT INTO `familywarremotefamily` VALUES ('4194317', '1001', '1', '8', '11');
INSERT INTO `familywarremotefamily` VALUES ('4194318', '1001', '16', '16', '');
INSERT INTO `familywarremotefamily` VALUES ('4194319', '1001', '1', '8', '7');
INSERT INTO `familywarremotefamily` VALUES ('4194320', '1001', '16', '16', '');

-- ----------------------------
-- Table structure for `qualifyingfixture`
-- ----------------------------
DROP TABLE IF EXISTS `qualifyingfixture`;
CREATE TABLE `qualifyingfixture` (
  `battletype` int(11) NOT NULL,
  `groupid` int(11) NOT NULL,
  `camp1familyid` bigint(20) NOT NULL,
  `camp2familyid` bigint(20) NOT NULL,
  `camp1serverid` int(11) default NULL,
  `camp2serverid` int(11) default NULL,
  `markfinish` bigint(20) NOT NULL default '0',
  `winnerfamilyid` bigint(20) default NULL,
  PRIMARY KEY  (`battletype`,`groupid`,`camp1familyid`,`camp2familyid`,`markfinish`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of qualifyingfixture
-- ----------------------------

-- ----------------------------
-- Table structure for `rareofficerrole`
-- ----------------------------
DROP TABLE IF EXISTS `rareofficerrole`;
CREATE TABLE `rareofficerrole` (
  `roleid` bigint(11) NOT NULL COMMENT '角色id',
  `serverid` int(11) NOT NULL COMMENT '所属服务器id',
  `name` varchar(255) NOT NULL COMMENT '角色名称',
  `camptype` int(11) NOT NULL COMMENT '阵营id',
  `cityid` int(11) NOT NULL COMMENT '城市id',
  `reputation` int(11) NOT NULL COMMENT '声望',
  `rareofficerid` int(11) NOT NULL COMMENT '稀有官职id',
  `flowerNum` int(11) NOT NULL COMMENT '被送花数',
  `jobid` int(11) NOT NULL COMMENT '职业id',
  `rank` int(11) NOT NULL COMMENT '排名',
  PRIMARY KEY  (`roleid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of rareofficerrole
-- ----------------------------

-- ----------------------------
-- Table structure for `rolebeinvite`
-- ----------------------------
DROP TABLE IF EXISTS `rolebeinvite`;
CREATE TABLE `rolebeinvite` (
  `roleid` bigint(20) NOT NULL COMMENT '角色ID',
  `bindinvitecode` varchar(255) character set utf8 collate utf8_bin default NULL COMMENT '绑定的邀请码',
  `status` smallint(1) NOT NULL default '0' COMMENT '领奖状态',
  PRIMARY KEY  (`roleid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of rolebeinvite
-- ----------------------------
INSERT INTO `rolebeinvite` VALUES ('4194305', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4194308', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4194310', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4194313', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4194315', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4194318', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4194321', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4194324', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4194325', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4194328', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4194329', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4194330', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4194331', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4194334', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4194343', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4194344', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4194346', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4194348', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4194350', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4194351', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4194356', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4194357', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4194358', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4194359', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4194363', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4194368', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4194369', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4194383', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4194388', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4194392', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4194399', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4194408', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4194425', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4194453', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4194457', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4194459', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4194465', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4194472', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4194486', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4194488', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4194493', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4194494', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4194496', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4194497', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4194501', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4194506', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4194508', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4194509', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4194510', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4194517', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4194518', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4194520', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4194522', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4194532', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4194536', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4194538', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4194544', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4194548', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4194549', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4194559', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4194561', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4194567', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4194570', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4194578', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4194634', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4194635', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4194642', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4194666', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4194698', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4194700', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4194709', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4194710', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4194711', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4194712', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4194715', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4194716', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4194717', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4194718', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4194721', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4194722', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4194723', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4194750', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4194754', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4194756', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4194759', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4194765', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4194766', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4194771', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4194772', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4194779', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4194788', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4194792', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4194799', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4194804', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4194816', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4194817', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4194818', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4194823', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4194824', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4194835', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4194840', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4194843', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4194860', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4194886', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4194898', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4194914', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4194920', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4194927', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4194954', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4194962', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4194977', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4194981', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4194994', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195002', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195004', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195026', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195040', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195058', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195059', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195084', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195089', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195103', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195106', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195111', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195131', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195137', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195139', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195145', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195152', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195153', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195157', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195161', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195162', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195173', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195174', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195184', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195185', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195188', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195191', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195206', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195208', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195209', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195210', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195211', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195212', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195213', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195218', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195235', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195250', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195251', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195252', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195281', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195307', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195310', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195313', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195314', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195315', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195322', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195326', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195331', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195337', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195357', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195358', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195360', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195369', '111618fi', '1');
INSERT INTO `rolebeinvite` VALUES ('4195383', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195388', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195391', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195392', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195401', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195408', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195420', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195421', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195424', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195440', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195446', '11155flS', '2');
INSERT INTO `rolebeinvite` VALUES ('4195454', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195465', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195470', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195483', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195500', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195502', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195506', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195510', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195551', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195552', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195553', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195554', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195557', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195558', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195559', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195561', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195575', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195586', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195594', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195611', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195620', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195636', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195646', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195687', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195695', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195699', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195725', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195729', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195755', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195764', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195768', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195769', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195771', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195776', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195778', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195779', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195780', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195788', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195792', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195795', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195807', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195815', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195820', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195821', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195829', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195830', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195832', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195837', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195844', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195848', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195849', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195855', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195856', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195857', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195860', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195864', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195865', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195875', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195880', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195888', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195890', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195891', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195892', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195895', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195896', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195900', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195906', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195911', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195913', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195914', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195915', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195918', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195919', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195920', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195921', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195925', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195933', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195934', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195935', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195938', '11102obj', '2');
INSERT INTO `rolebeinvite` VALUES ('4195939', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195940', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195941', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195942', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195943', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195944', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195945', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195947', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195950', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195951', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195952', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195953', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195954', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195955', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195956', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195957', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195958', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195959', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195960', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195961', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195962', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195963', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195964', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195965', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195966', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195967', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195968', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195969', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195970', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195971', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195972', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195973', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195974', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195975', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195976', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195977', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195978', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195979', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195980', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195981', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195982', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195983', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195984', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195985', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195986', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195987', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195988', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195989', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195990', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195991', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4195994', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4196014', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4196135', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4196151', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4196154', null, '0');
INSERT INTO `rolebeinvite` VALUES ('4196196', null, '0');
INSERT INTO `rolebeinvite` VALUES ('41947234430', null, '0');
INSERT INTO `rolebeinvite` VALUES ('41947236105', null, '0');

-- ----------------------------
-- Table structure for `rolefamilyescort`
-- ----------------------------
DROP TABLE IF EXISTS `rolefamilyescort`;
CREATE TABLE `rolefamilyescort` (
  `roleid` bigint(3) NOT NULL default '0',
  `escorttime` int(3) default NULL,
  `robtime` int(3) default NULL,
  PRIMARY KEY  (`roleid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of rolefamilyescort
-- ----------------------------

-- ----------------------------
-- Table structure for `rolefightingmaster`
-- ----------------------------
DROP TABLE IF EXISTS `rolefightingmaster`;
CREATE TABLE `rolefightingmaster` (
  `roleid` bigint(20) NOT NULL,
  `fightscore` int(11) NOT NULL default '0' COMMENT '战力分数',
  `disscore` int(11) NOT NULL default '0' COMMENT '表现积分',
  `fightcount` int(11) NOT NULL default '0' COMMENT '战斗次数',
  `ymdh` int(11) NOT NULL default '0' COMMENT '最后一次战斗日期',
  `fighttimes` smallint(6) NOT NULL default '0' COMMENT '每天战斗次数',
  `matchrobotpersent` int(11) NOT NULL default '0' COMMENT '机器人的匹配概率',
  `seqwinorfailed` int(11) NOT NULL default '0' COMMENT '连赢或者连输 1xxx 表示连赢多少场 2xxx表示连输多少场',
  `rank` int(11) NOT NULL default '0' COMMENT '排名',
  `name` varchar(64) NOT NULL default '' COMMENT '名字',
  `serverid` int(11) NOT NULL COMMENT '服务id',
  `level` int(11) NOT NULL COMMENT '等级',
  `fiveaward` tinyint(4) NOT NULL COMMENT '五战奖励 0 未领取 1已领取',
  `lastrankawardymd` int(11) NOT NULL COMMENT '最后一次排行榜奖励时间',
  `rankup` varchar(64) NOT NULL default '' COMMENT '跨榜id',
  PRIMARY KEY  (`roleid`),
  KEY `rank` USING BTREE (`rank`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of rolefightingmaster
-- ----------------------------
INSERT INTO `rolefightingmaster` VALUES ('4194305', '294675', '156', '1', '2017062915', '0', '50', '1001', '87', '韩遇殊', '1', '150', '0', '2017071200', '911');
INSERT INTO `rolefightingmaster` VALUES ('4194310', '225749', '70', '1', '2017062709', '0', '50', '1001', '0', '施离', '1', '120', '0', '2017062415', '911');
INSERT INTO `rolefightingmaster` VALUES ('4194312', '1844209', '1189', '9', '2017061517', '0', '70', '2002', '4', '栾寄愁', '1', '100', '0', '2017071200', '913');
INSERT INTO `rolefightingmaster` VALUES ('4194313', '2226714', '937', '34', '2017063009', '0', '30', '1001', '7', 'MMMMMMMMMMMM', '1', '120', '0', '2017071200', '913');
INSERT INTO `rolefightingmaster` VALUES ('4194315', '1452603', '526', '13', '2017062714', '0', '85', '2003', '17', '桑与竹', '1', '116', '0', '2017071200', '912');
INSERT INTO `rolefightingmaster` VALUES ('4194317', '42953', '57', '2', '2017052618', '2', '60', '2001', '0', '桑七苏', '1', '48', '0', '2017052618', '911');
INSERT INTO `rolefightingmaster` VALUES ('4194318', '1449624', '716', '2', '2017070417', '0', '70', '2002', '5', '天下第一', '1', '150', '0', '2017071200', '');
INSERT INTO `rolefightingmaster` VALUES ('4194319', '40685', '70', '1', '2017041818', '0', '50', '1001', '0', '全服第一', '1', '45', '0', '2017041418', '');
INSERT INTO `rolefightingmaster` VALUES ('4194320', '241055', '233', '4', '2017042009', '0', '50', '1004', '41', '百里七', '1', '88', '0', '2017071200', '911');
INSERT INTO `rolefightingmaster` VALUES ('4194325', '163647', '181', '2', '2017070815', '0', '50', '1002', '73', '万剑归宗剑尊', '1', '120', '0', '2017071200', '911');
INSERT INTO `rolefightingmaster` VALUES ('4194328', '88514', '34', '2', '2017063010', '0', '70', '2002', '0', '000000000000', '1', '150', '0', '2017063010', '');
INSERT INTO `rolefightingmaster` VALUES ('4194329', '283930', '236', '76', '2017071109', '0', '50', '1001', '38', '上官青岚', '1', '92', '0', '2017071200', '911');
INSERT INTO `rolefightingmaster` VALUES ('4194331', '101559', '513', '14', '2017062219', '0', '60', '2001', '20', '陈长楠', '1', '120', '0', '2017071200', '912+911');
INSERT INTO `rolefightingmaster` VALUES ('4194335', '256569', '128', '0', '2017032814', '0', '50', '0', '0', '顾少阁', '1', '87', '0', '2017060700', '');
INSERT INTO `rolefightingmaster` VALUES ('4194338', '28297', '64', '1', '2017041118', '0', '50', '1001', '0', '韩仲楠', '1', '39', '0', '2017041118', '');
INSERT INTO `rolefightingmaster` VALUES ('4194341', '163428', '374', '18', '2017052317', '0', '60', '2001', '31', '赵子鸾', '1', '80', '0', '2017071200', '912+911');
INSERT INTO `rolefightingmaster` VALUES ('4194342', '213684', '105', '1', '2017032210', '0', '50', '1001', '0', '顾安景', '1', '149', '0', '2017032210', '');
INSERT INTO `rolefightingmaster` VALUES ('4194343', '437945', '206', '6', '2017071011', '0', '60', '2001', '61', 'AAAAAAAAAAAA', '1', '130', '0', '2017071200', '911');
INSERT INTO `rolefightingmaster` VALUES ('4194346', '44929', '18', '1', '2017071114', '0', '60', '2001', '0', '平宁少师', '1', '45', '0', '2017071114', '');
INSERT INTO `rolefightingmaster` VALUES ('4194350', '451715', '368', '119', '2017071210', '0', '175', '2009', '33', '颜亦水', '1', '120', '0', '2017071200', '912+913+911');
INSERT INTO `rolefightingmaster` VALUES ('4194351', '128622', '207', '8', '2017070611', '0', '50', '1001', '60', '屈镇南', '1', '100', '0', '2017071200', '911');
INSERT INTO `rolefightingmaster` VALUES ('4194352', '83912', '33', '2', '2017060810', '0', '70', '2002', '0', '弔弔', '1', '150', '0', '2017060810', '');
INSERT INTO `rolefightingmaster` VALUES ('4194356', '75198', '87', '1', '2017070409', '0', '50', '1001', '0', '方微', '1', '84', '0', '2017062817', '911');
INSERT INTO `rolefightingmaster` VALUES ('4194357', '397442', '300', '19', '2017062010', '0', '65', '2001', '54', '平宁绿儿', '1', '100', '0', '2017071200', '912+911');
INSERT INTO `rolefightingmaster` VALUES ('4194359', '290355', '257', '18', '2017071115', '0', '130', '2008', '27', '冷与真', '1', '79', '0', '2017071200', '911');
INSERT INTO `rolefightingmaster` VALUES ('4194363', '552486', '276', '0', '2017071208', '0', '50', '0', '7', '鬼谷子下棋棋', '1', '120', '0', '2017071200', '');
INSERT INTO `rolefightingmaster` VALUES ('4194364', '283789', '186', '2', '2017061316', '0', '60', '2001', '68', '姒入', '1', '92', '0', '2017071200', '911');
INSERT INTO `rolefightingmaster` VALUES ('4194369', '655206', '63', '4', '2017071109', '0', '60', '2001', '0', '芈令侠', '1', '93', '0', '2017070511', '911');
INSERT INTO `rolefightingmaster` VALUES ('4194371', '30111', '65', '1', '2017061914', '0', '50', '1001', '0', '萧千兮', '1', '40', '0', '2017061914', '');
INSERT INTO `rolefightingmaster` VALUES ('4194373', '998768', '513', '2', '2017060815', '0', '40', '1002', '18', '这是六个字吗', '1', '100', '0', '2017071200', '912');
INSERT INTO `rolefightingmaster` VALUES ('4194383', '214834', '60', '1', '2017062217', '0', '50', '1001', '0', '万若梨', '1', '130', '0', '2017062217', '911');
INSERT INTO `rolefightingmaster` VALUES ('4194392', '50471', '1', '4', '2017071016', '0', '90', '2004', '0', '曲一', '1', '100', '0', '2017071016', '');
INSERT INTO `rolefightingmaster` VALUES ('4194409', '668176', '311', '6', '2017042211', '0', '50', '1001', '50', '颜宜逐', '1', '120', '0', '2017071200', '912+911');
INSERT INTO `rolefightingmaster` VALUES ('4194411', '32134', '66', '1', '2017051509', '0', '50', '1001', '0', '乔叔真', '1', '42', '0', '2017051317', '911');
INSERT INTO `rolefightingmaster` VALUES ('4194414', '38606', '15', '1', '2017050910', '0', '60', '2001', '0', '端木牧情', '1', '40', '0', '2017050910', '');
INSERT INTO `rolefightingmaster` VALUES ('4194416', '36237', '118', '2', '2017050811', '0', '50', '1002', '0', '顾蕴岩', '1', '41', '0', '2017050614', '911');
INSERT INTO `rolefightingmaster` VALUES ('4194425', '471272', '48', '6', '2017071112', '0', '110', '2006', '0', 'null', '1', '100', '0', '2017070514', '');
INSERT INTO `rolefightingmaster` VALUES ('4194430', '41015', '70', '1', '2017032414', '0', '50', '1001', '0', '燕晴陌', '1', '79', '0', '2017032414', '');
INSERT INTO `rolefightingmaster` VALUES ('4194436', '25158', '1', '3', '2017032511', '3', '80', '2003', '0', '杨非', '1', '83', '0', '2017032511', '');
INSERT INTO `rolefightingmaster` VALUES ('4194438', '46037', '19', '1', '2017032416', '1', '60', '2001', '0', '慕思', '1', '41', '0', '2017032416', '');
INSERT INTO `rolefightingmaster` VALUES ('4194444', '41938', '16', '1', '2017042814', '0', '60', '2001', '0', '夙相川', '1', '46', '0', '2017042620', '');
INSERT INTO `rolefightingmaster` VALUES ('4194445', '32375', '12', '1', '2017061410', '0', '60', '2001', '0', '年瑞卿', '1', '40', '0', '2017061410', '');
INSERT INTO `rolefightingmaster` VALUES ('4194446', '62753', '70', '2', '2017042514', '0', '50', '1001', '0', '第五遇诗', '1', '57', '0', '2017042109', '');
INSERT INTO `rolefightingmaster` VALUES ('4194457', '296593', '266', '6', '2017062709', '0', '50', '1005', '20', '无视1', '1', '73', '0', '2017071200', '911');
INSERT INTO `rolefightingmaster` VALUES ('4194459', '889879', '1130', '46', '2017052010', '0', '30', '1036', '5', '李一兮a', '1', '100', '0', '2017071200', '912+913');
INSERT INTO `rolefightingmaster` VALUES ('4194464', '43854', '17', '1', '2017050920', '0', '60', '2001', '0', '易逸石', '1', '45', '0', '2017050309', '');
INSERT INTO `rolefightingmaster` VALUES ('4194465', '812354', '356', '22', '2017071208', '0', '55', '2001', '34', '南宫辞言', '1', '100', '0', '2017071200', '912+911');
INSERT INTO `rolefightingmaster` VALUES ('4194470', '43268', '17', '1', '2017032515', '1', '60', '2001', '0', '桑丘西夫', '1', '45', '0', '2017032515', '');
INSERT INTO `rolefightingmaster` VALUES ('4194472', '203068', '16', '12', '2017071123', '0', '120', '2007', '0', '栾展楠栾展楠', '1', '90', '0', '2017070716', '911');
INSERT INTO `rolefightingmaster` VALUES ('4194479', '39044', '15', '1', '2017051217', '0', '60', '2001', '0', '苍非情', '1', '43', '0', '2017051217', '');
INSERT INTO `rolefightingmaster` VALUES ('4194493', '404727', '372', '64', '2017071207', '0', '40', '1003', '32', '幻一', '1', '100', '0', '2017071200', '912+911');
INSERT INTO `rolefightingmaster` VALUES ('4194494', '3125550', '792', '27', '2017071210', '0', '40', '1014', '1', '白七亭', '1', '130', '0', '2017071200', '912+911');
INSERT INTO `rolefightingmaster` VALUES ('4194495', '110757', '155', '2', '2017052318', '0', '50', '1002', '89', '宋晴', '1', '45', '0', '2017071200', '911');
INSERT INTO `rolefightingmaster` VALUES ('4194496', '348796', '277', '2', '2017071208', '0', '50', '1002', '6', '龙二', '1', '100', '0', '2017071200', '911');
INSERT INTO `rolefightingmaster` VALUES ('4194497', '193560', '267', '6', '2017071109', '0', '50', '1001', '16', '姜镇', '1', '89', '0', '2017071200', '911');
INSERT INTO `rolefightingmaster` VALUES ('4194499', '13337', '56', '1', '2017052620', '1', '50', '1001', '0', '祝仲景', '1', '23', '0', '2017052620', '911');
INSERT INTO `rolefightingmaster` VALUES ('4194500', '57293', '12', '4', '2017052310', '0', '90', '2004', '0', '年倾梧', '1', '150', '0', '2017052310', '');
INSERT INTO `rolefightingmaster` VALUES ('4194501', '357415', '244', '9', '2017071118', '0', '60', '2001', '30', '张三', '1', '100', '0', '2017071200', '911');
INSERT INTO `rolefightingmaster` VALUES ('4194506', '350491', '320', '5', '2017071117', '0', '50', '1002', '45', '李四', '1', '100', '0', '2017071200', '912+911');
INSERT INTO `rolefightingmaster` VALUES ('4194509', '31088', '65', '1', '2017062415', '0', '50', '1001', '0', '闻心兰', '1', '41', '0', '2017062415', '');
INSERT INTO `rolefightingmaster` VALUES ('4194510', '46919', '19', '1', '2017071110', '0', '60', '2001', '0', '花五棠', '1', '150', '0', '2017071110', '');
INSERT INTO `rolefightingmaster` VALUES ('4194513', '182183', '115', '3', '2017050409', '0', '60', '2001', '0', '乔若', '1', '57', '0', '2017050318', '911');
INSERT INTO `rolefightingmaster` VALUES ('4194514', '57642', '128', '2', '2017042817', '0', '50', '1002', '0', '彦微枫', '1', '120', '0', '2017060700', '911');
INSERT INTO `rolefightingmaster` VALUES ('4194517', '29605', '10', '1', '2017062917', '0', '60', '2001', '0', '左希', '1', '41', '0', '2017062917', '');
INSERT INTO `rolefightingmaster` VALUES ('4194518', '280470', '235', '2', '2017062717', '0', '50', '1002', '40', '夏语言', '1', '100', '0', '2017071200', '911');
INSERT INTO `rolefightingmaster` VALUES ('4194520', '350206', '170', '2', '2017071117', '0', '70', '2002', '76', '凤五', '1', '100', '0', '2017071200', '');
INSERT INTO `rolefightingmaster` VALUES ('4194522', '349394', '211', '5', '2017071119', '0', '80', '2003', '59', '六六六', '1', '100', '0', '2017071200', '911');
INSERT INTO `rolefightingmaster` VALUES ('4194523', '34842', '67', '1', '2017032816', '0', '50', '1001', '0', '白百', '1', '41', '0', '2017032518', '');
INSERT INTO `rolefightingmaster` VALUES ('4194530', '1005972', '1314', '59', '2017050809', '0', '50', '2001', '2', '凌息真', '1', '100', '0', '2017071200', '912+913+911');
INSERT INTO `rolefightingmaster` VALUES ('4194532', '354981', '273', '3', '2017071119', '0', '60', '2001', '9', '鬼七', '1', '120', '0', '2017071200', '911');
INSERT INTO `rolefightingmaster` VALUES ('4194536', '358257', '395', '6', '2017071119', '0', '60', '2001', '26', '小八', '1', '120', '0', '2017071200', '912+911');
INSERT INTO `rolefightingmaster` VALUES ('4194538', '31278', '11', '1', '2017062415', '0', '60', '2001', '0', '司空丹溪', '1', '42', '0', '2017062415', '');
INSERT INTO `rolefightingmaster` VALUES ('4194540', '20512', '10', '0', '2017041715', '0', '50', '0', '0', '甄其', '1', '40', '0', '2017041715', '');
INSERT INTO `rolefightingmaster` VALUES ('4194548', '32864', '195', '8', '2017071116', '0', '60', '2001', '66', '蔺鹤真', '1', '43', '0', '2017071200', '911');
INSERT INTO `rolefightingmaster` VALUES ('4194549', '353498', '176', '0', '2017071119', '0', '50', '0', '75', '九尊', '1', '100', '0', '2017071200', '');
INSERT INTO `rolefightingmaster` VALUES ('4194556', '158994', '79', '0', '2017041811', '0', '50', '0', '0', '百里仲生', '1', '120', '0', '2017041811', '');
INSERT INTO `rolefightingmaster` VALUES ('4194559', '1401487', '270', '25', '2017071111', '0', '125', '2007', '11', '啊啊啊啊啊啊', '1', '120', '0', '2017071200', '912+911');
INSERT INTO `rolefightingmaster` VALUES ('4194561', '349357', '129', '9', '2017071119', '0', '140', '2009', '0', '十妹', '1', '100', '0', '2017071119', '');
INSERT INTO `rolefightingmaster` VALUES ('4194571', '81232', '232', '2', '2017041515', '0', '50', '1002', '42', '东方从澜', '1', '83', '0', '2017071200', '911');
INSERT INTO `rolefightingmaster` VALUES ('4194572', '265411', '227', '3', '2017032715', '0', '50', '1002', '44', '上官停塘', '1', '82', '0', '2017071200', '911');
INSERT INTO `rolefightingmaster` VALUES ('4194578', '814193', '442', '26', '2017071121', '0', '70', '2002', '23', '独孤丹', '1', '109', '0', '2017071200', '912+911');
INSERT INTO `rolefightingmaster` VALUES ('4194581', '42582', '121', '2', '2017032910', '0', '50', '1002', '0', '老司机开车了', '1', '45', '0', '2017052400', '911');
INSERT INTO `rolefightingmaster` VALUES ('4194598', '38229', '68', '1', '2017061310', '0', '50', '1001', '0', '齐细词', '1', '45', '0', '2017061310', '911');
INSERT INTO `rolefightingmaster` VALUES ('4194600', '13769', '2', '1', '2017032809', '1', '60', '2001', '0', '荆蕴', '1', '45', '0', '2017032809', '');
INSERT INTO `rolefightingmaster` VALUES ('4194602', '35656', '13', '1', '2017032809', '1', '60', '2001', '0', '林九曦', '1', '118', '0', '2017032809', '');
INSERT INTO `rolefightingmaster` VALUES ('4194604', '16760', '4', '1', '2017032809', '1', '60', '2001', '0', '方停亭', '1', '55', '0', '2017032809', '');
INSERT INTO `rolefightingmaster` VALUES ('4194605', '46221', '19', '1', '2017053114', '0', '60', '2001', '0', '宁墨棋', '1', '150', '0', '2017053114', '');
INSERT INTO `rolefightingmaster` VALUES ('4194635', '628525', '128', '3', '2017071210', '0', '60', '2001', '0', '甄五兰', '1', '100', '0', '2017071210', '911');
INSERT INTO `rolefightingmaster` VALUES ('4194636', '33192', '66', '1', '2017032915', '0', '50', '1001', '0', '秋绿隐', '1', '42', '0', '2017032915', '');
INSERT INTO `rolefightingmaster` VALUES ('4194638', '36975', '213', '5', '2017032820', '5', '60', '2001', '57', '平宁墨生', '1', '46', '1', '2017071200', '911');
INSERT INTO `rolefightingmaster` VALUES ('4194642', '29332', '14', '0', '2017063016', '0', '50', '0', '0', '白倾', '1', '83', '0', '2017063016', '');
INSERT INTO `rolefightingmaster` VALUES ('4194650', '492553', '242', '1', '2017032816', '0', '60', '2001', '33', '司空过雪', '1', '90', '0', '2017071200', '');
INSERT INTO `rolefightingmaster` VALUES ('4194663', '117936', '108', '1', '2017042911', '1', '50', '1001', '0', '荆治画', '1', '40', '0', '2017042911', '911');
INSERT INTO `rolefightingmaster` VALUES ('4194666', '49818', '19', '1', '2017062211', '0', '60', '2001', '0', '甄若廷', '1', '100', '0', '2017062211', '');
INSERT INTO `rolefightingmaster` VALUES ('4194677', '271445', '242', '4', '2017032209', '0', '50', '1004', '34', '夺宝1号', '1', '148', '0', '2017071200', '911');
INSERT INTO `rolefightingmaster` VALUES ('4194678', '115996', '127', '3', '2017032108', '0', '50', '1002', '0', '夺宝2号', '1', '62', '0', '2017060700', '911');
INSERT INTO `rolefightingmaster` VALUES ('4194679', '69729', '122', '2', '2017031409', '0', '50', '1002', '0', '夺宝3号', '1', '60', '0', '2017052400', '911');
INSERT INTO `rolefightingmaster` VALUES ('4194688', '43598', '21', '0', '2017032819', '0', '50', '0', '0', '夺宝6号', '1', '60', '0', '2017032819', '');
INSERT INTO `rolefightingmaster` VALUES ('4194697', '32572', '66', '1', '2017042810', '0', '50', '1001', '0', '乐正辞英', '1', '41', '0', '2017042810', '');
INSERT INTO `rolefightingmaster` VALUES ('4194698', '232103', '593', '69', '2017071115', '0', '115', '2005', '11', '万俟灵襄', '1', '87', '0', '2017071200', '912+911');
INSERT INTO `rolefightingmaster` VALUES ('4194700', '1807439', '570', '18', '2017071109', '0', '40', '1001', '12', '桑书棠', '1', '111', '0', '2017071200', '912+911');
INSERT INTO `rolefightingmaster` VALUES ('4194703', '37883', '68', '1', '2017050211', '0', '50', '1001', '0', '上官振隐', '1', '44', '0', '2017050211', '');
INSERT INTO `rolefightingmaster` VALUES ('4194704', '57389', '267', '5', '2017050809', '0', '50', '1005', '18', '东方远末', '1', '56', '0', '2017071200', '911');
INSERT INTO `rolefightingmaster` VALUES ('4194721', '389284', '244', '1', '2017071120', '0', '50', '1001', '29', '南宫以流', '1', '100', '0', '2017071200', '911');
INSERT INTO `rolefightingmaster` VALUES ('4194723', '363194', '181', '0', '2017071207', '0', '50', '0', '72', '赵以隐', '1', '120', '0', '2017071200', '');
INSERT INTO `rolefightingmaster` VALUES ('4194730', '39053', '265', '6', '2017043009', '0', '50', '1001', '25', '叶忆', '1', '45', '0', '2017071200', '911');
INSERT INTO `rolefightingmaster` VALUES ('4194740', '35362', '67', '1', '2017032916', '1', '50', '1001', '0', '兔兔', '1', '42', '0', '2017032916', '');
INSERT INTO `rolefightingmaster` VALUES ('4194741', '38808', '69', '1', '2017041809', '0', '50', '1001', '0', '荆三兮', '1', '44', '0', '2017041809', '');
INSERT INTO `rolefightingmaster` VALUES ('4194750', '269631', '62', '2', '2017070815', '0', '50', '1001', '0', '白可景', '1', '87', '0', '2017070614', '911');
INSERT INTO `rolefightingmaster` VALUES ('4194755', '35356', '67', '1', '2017033008', '1', '50', '1001', '0', '诸葛六苏', '1', '42', '0', '2017033008', '');
INSERT INTO `rolefightingmaster` VALUES ('4194756', '762908', '292', '47', '2017071109', '1', '120', '2006', '2', '凌治枫', '1', '125', '0', '2017071200', '912+911');
INSERT INTO `rolefightingmaster` VALUES ('4194765', '311758', '243', '29', '2017071110', '0', '185', '2012', '32', '万五蓝', '1', '102', '0', '2017071200', '912+911');
INSERT INTO `rolefightingmaster` VALUES ('4194767', '100436', '329', '6', '2017050617', '0', '50', '1006', '43', '杨离隐', '1', '62', '0', '2017071200', '912+911');
INSERT INTO `rolefightingmaster` VALUES ('4194774', '31384', '307', '7', '2017042914', '0', '50', '1006', '51', '纳兰蕴诗', '1', '47', '0', '2017071200', '912+911');
INSERT INTO `rolefightingmaster` VALUES ('4194775', '56360', '116', '2', '2017051311', '0', '50', '1002', '0', '齐剑言', '1', '53', '0', '2017051009', '911');
INSERT INTO `rolefightingmaster` VALUES ('4194778', '165558', '82', '0', '2017033109', '0', '50', '0', '0', '凤过雪', '1', '78', '0', '2017033020', '');
INSERT INTO `rolefightingmaster` VALUES ('4194784', '412691', '557', '12', '2017041014', '0', '50', '1012', '13', '秦筱', '1', '69', '0', '2017071200', '912+911');
INSERT INTO `rolefightingmaster` VALUES ('4194785', '66034', '165', '3', '2017050918', '0', '50', '1003', '81', '陈晓为', '1', '63', '0', '2017071200', '911');
INSERT INTO `rolefightingmaster` VALUES ('4194788', '722417', '241', '6', '2017071116', '0', '50', '1001', '36', '池又川', '1', '120', '0', '2017071200', '911');
INSERT INTO `rolefightingmaster` VALUES ('4194789', '32413', '66', '1', '2017052216', '0', '50', '1001', '0', '林觅', '1', '40', '0', '2017052216', '911');
INSERT INTO `rolefightingmaster` VALUES ('4194792', '28430', '64', '1', '2017071209', '0', '50', '1001', '0', '鬼谷与水', '1', '43', '0', '2017071209', '911');
INSERT INTO `rolefightingmaster` VALUES ('4194794', '20469', '55', '2', '2017052617', '2', '60', '2001', '0', '洛逸袖', '1', '36', '0', '2017052617', '911');
INSERT INTO `rolefightingmaster` VALUES ('4194795', '25157', '7', '1', '2017052618', '1', '60', '2001', '0', '吴余衣', '1', '26', '0', '2017052618', '');
INSERT INTO `rolefightingmaster` VALUES ('4194799', '96522', '24', '5', '2017071117', '0', '100', '2005', '0', '甄蕴岚', '1', '94', '0', '2017071117', '');
INSERT INTO `rolefightingmaster` VALUES ('4194804', '33620', '66', '1', '2017071015', '0', '50', '1001', '0', '游语尘', '1', '43', '0', '2017071015', '911');
INSERT INTO `rolefightingmaster` VALUES ('4194816', '42688', '269', '5', '2017062714', '0', '50', '1005', '13', '施星歌', '1', '48', '0', '2017071200', '911');
INSERT INTO `rolefightingmaster` VALUES ('4194818', '84263', '92', '1', '2017062009', '0', '50', '1001', '0', '甄星雨', '1', '44', '0', '2017061917', '911');
INSERT INTO `rolefightingmaster` VALUES ('4194822', '34018', '267', '5', '2017041410', '0', '50', '1005', '19', '顾南堂', '1', '43', '0', '2017071200', '911');
INSERT INTO `rolefightingmaster` VALUES ('4194823', '250343', '738', '26', '2017062716', '0', '40', '1004', '3', '青阳寄水', '1', '93', '0', '2017071200', '912+911');
INSERT INTO `rolefightingmaster` VALUES ('4194833', '36419', '68', '1', '2017040120', '1', '50', '1001', '0', '柳从棠', '1', '43', '0', '2017040120', '911');
INSERT INTO `rolefightingmaster` VALUES ('4194835', '385847', '778', '18', '2017071210', '0', '50', '1018', '2', '左以', '1', '73', '0', '2017071200', '912+911');
INSERT INTO `rolefightingmaster` VALUES ('4194843', '50493', '25', '0', '2017071110', '0', '50', '0', '0', '蔺一画', '1', '84', '0', '2017071110', '');
INSERT INTO `rolefightingmaster` VALUES ('4194855', '65653', '49', '5', '2017061409', '0', '90', '2004', '0', '鬼谷青末', '1', '75', '0', '2017061409', '911');
INSERT INTO `rolefightingmaster` VALUES ('4194857', '35359', '17', '0', '2017040210', '0', '50', '0', '0', '夙右', '1', '117', '0', '2017040210', '');
INSERT INTO `rolefightingmaster` VALUES ('4194858', '43178', '71', '1', '2017050416', '0', '50', '1001', '0', '剑尊', '1', '42', '0', '2017050416', '911');
INSERT INTO `rolefightingmaster` VALUES ('4194868', '36143', '68', '1', '2017040216', '1', '50', '1001', '0', '我跑得最慢', '1', '42', '0', '2017040216', '911');
INSERT INTO `rolefightingmaster` VALUES ('4194878', '48276', '74', '1', '2017041314', '0', '50', '1001', '0', '花以亭', '1', '48', '0', '2017041314', '911');
INSERT INTO `rolefightingmaster` VALUES ('4194880', '39070', '59', '3', '2017060110', '0', '50', '1001', '0', '楚碧楼', '1', '46', '0', '2017060110', '911');
INSERT INTO `rolefightingmaster` VALUES ('4194884', '12712', '56', '1', '2017052617', '1', '50', '1001', '0', '梁飞情', '1', '23', '0', '2017052617', '911');
INSERT INTO `rolefightingmaster` VALUES ('4194885', '35819', '57', '3', '2017061917', '0', '70', '2002', '0', '洛停恭', '1', '45', '0', '2017061917', '911');
INSERT INTO `rolefightingmaster` VALUES ('4194898', '213555', '72', '4', '2017071119', '0', '80', '2003', '0', '杜永', '1', '120', '0', '2017071119', '911');
INSERT INTO `rolefightingmaster` VALUES ('4194899', '34042', '67', '1', '2017040511', '1', '50', '1001', '0', '宁息凉', '1', '46', '0', '2017040511', '911');
INSERT INTO `rolefightingmaster` VALUES ('4194905', '75473', '332', '12', '2017042809', '0', '115', '2005', '42', '颜遇月', '1', '100', '0', '2017071200', '912+911');
INSERT INTO `rolefightingmaster` VALUES ('4194906', '110974', '276', '6', '2017061409', '0', '60', '2001', '8', '蜈蚣丸', '1', '66', '0', '2017071200', '911');
INSERT INTO `rolefightingmaster` VALUES ('4194911', '35077', '117', '2', '2017040514', '2', '50', '1002', '0', '桑筱', '1', '42', '0', '2017040514', '911');
INSERT INTO `rolefightingmaster` VALUES ('4194912', '90323', '168', '3', '2017053115', '0', '50', '1003', '80', '栾倾曦', '1', '61', '0', '2017071200', '911');
INSERT INTO `rolefightingmaster` VALUES ('4194914', '553499', '326', '1', '2017071200', '0', '50', '1001', '44', '萧右', '1', '95', '0', '2017071200', '912');
INSERT INTO `rolefightingmaster` VALUES ('4194918', '35367', '117', '2', '2017040517', '2', '50', '1002', '0', '辛辞夫', '1', '42', '0', '2017040517', '911');
INSERT INTO `rolefightingmaster` VALUES ('4194920', '33602', '66', '1', '2017062714', '0', '50', '1001', '0', '方亦', '1', '40', '0', '2017062714', '911');
INSERT INTO `rolefightingmaster` VALUES ('4194926', '13465', '1', '5', '2017040521', '5', '100', '2005', '0', '欧阳与树', '1', '44', '0', '2017040521', '');
INSERT INTO `rolefightingmaster` VALUES ('4194933', '39115', '169', '3', '2017042817', '0', '50', '1003', '78', '闻人宝', '1', '49', '0', '2017071200', '911');
INSERT INTO `rolefightingmaster` VALUES ('4194947', '77858', '318', '6', '2017060619', '0', '50', '1006', '46', '楚南恭', '1', '58', '0', '2017071200', '912+911');
INSERT INTO `rolefightingmaster` VALUES ('4194962', '93500', '710', '56', '2017062415', '0', '40', '1001', '6', '甄牧堂', '1', '66', '0', '2017071200', '912+911');
INSERT INTO `rolefightingmaster` VALUES ('4194975', '24012', '62', '1', '2017050920', '1', '50', '1001', '0', '姬六真', '1', '49', '0', '2017050920', '911');
INSERT INTO `rolefightingmaster` VALUES ('4194977', '48684', '74', '1', '2017071110', '0', '50', '1001', '0', '瓜瓜瓜', '1', '83', '0', '2017070718', '911');
INSERT INTO `rolefightingmaster` VALUES ('4194981', '202323', '154', '5', '2017071110', '0', '50', '1002', '90', '万镇廷', '1', '98', '0', '2017071200', '911');
INSERT INTO `rolefightingmaster` VALUES ('4194983', '28131', '64', '1', '2017041009', '1', '50', '1001', '0', '慕星雪', '1', '42', '0', '2017041009', '911');
INSERT INTO `rolefightingmaster` VALUES ('4194999', '47952', '272', '5', '2017041020', '5', '50', '1005', '10', '南宫与之', '1', '49', '0', '2017071200', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195015', '72426', '727', '21', '2017041317', '0', '40', '1001', '4', '栾季', '1', '57', '0', '2017071200', '912+911');
INSERT INTO `rolefightingmaster` VALUES ('4195026', '484227', '162', '4', '2017071114', '0', '50', '1002', '83', '施微', '1', '101', '0', '2017071200', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195030', '164461', '160', '3', '2017041209', '0', '50', '1002', '85', '屈息', '1', '56', '0', '2017071200', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195047', '33571', '66', '1', '2017041316', '1', '50', '1001', '0', '方墨霜', '1', '42', '0', '2017041316', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195050', '125987', '58', '1', '2017041810', '0', '60', '2001', '0', '蔺九凡', '1', '42', '0', '2017041311', '');
INSERT INTO `rolefightingmaster` VALUES ('4195052', '31300', '65', '1', '2017041409', '0', '50', '1001', '0', '莫小兮', '1', '40', '0', '2017041315', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195059', '621902', '61', '12', '2017063009', '0', '60', '2001', '0', '诸葛九襄', '1', '119', '0', '2017063009', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195066', '25495', '62', '1', '2017042810', '0', '50', '1001', '0', '青阳觅雨', '1', '40', '0', '2017042810', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195096', '41562', '270', '5', '2017050511', '0', '50', '1005', '12', '齐过', '1', '42', '0', '2017071200', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195099', '34319', '112', '2', '2017042809', '0', '50', '1002', '0', '屈扬蓝', '1', '48', '0', '2017042809', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195106', '56533', '78', '1', '2017062015', '0', '50', '1001', '0', '欧阳落言', '1', '42', '0', '2017062015', '');
INSERT INTO `rolefightingmaster` VALUES ('4195109', '1118270', '224', '3', '2017042713', '0', '50', '1001', '45', '欧朱', '1', '100', '0', '2017071200', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195111', '36268', '14', '1', '2017070409', '0', '60', '2001', '0', '乔心卿', '1', '41', '0', '2017070409', '');
INSERT INTO `rolefightingmaster` VALUES ('4195112', '77326', '63', '2', '2017041514', '2', '60', '2001', '0', '你疯了嘛', '1', '57', '0', '2017041514', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195117', '35105', '67', '1', '2017051010', '0', '50', '1001', '0', '东方逸河', '1', '41', '0', '2017051010', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195120', '33876', '66', '1', '2017050515', '0', '50', '1001', '0', '林息堂', '1', '41', '0', '2017050515', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195129', '34629', '13', '1', '2017041909', '0', '60', '2001', '0', '鱼辞冰', '1', '41', '0', '2017041909', '');
INSERT INTO `rolefightingmaster` VALUES ('4195131', '36448', '68', '1', '2017062011', '0', '50', '1001', '0', '琴倾石', '1', '100', '0', '2017061919', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195132', '34181', '67', '1', '2017042915', '0', '50', '1001', '0', '阮蕴', '1', '41', '0', '2017042710', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195134', '34565', '67', '1', '2017041710', '1', '50', '1001', '0', '左飞', '1', '41', '0', '2017041710', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195137', '619091', '314', '7', '2017071118', '0', '50', '1002', '48', '万振情', '1', '93', '0', '2017071200', '912+911');
INSERT INTO `rolefightingmaster` VALUES ('4195139', '265473', '67', '1', '2017062609', '0', '50', '1001', '0', '百里剑霜', '1', '65', '0', '2017062309', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195140', '105783', '102', '1', '2017060215', '0', '50', '1001', '0', '杨逸珠', '1', '40', '0', '2017060215', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195141', '33431', '12', '1', '2017041820', '0', '60', '2001', '0', '李西鸾', '1', '41', '0', '2017041718', '');
INSERT INTO `rolefightingmaster` VALUES ('4195144', '33702', '1', '3', '2017050311', '0', '80', '2003', '0', '夏晓泉', '1', '47', '0', '2017050311', '');
INSERT INTO `rolefightingmaster` VALUES ('4195145', '588984', '393', '13', '2017070316', '0', '40', '1001', '27', '苍长景', '1', '97', '0', '2017071200', '912+911');
INSERT INTO `rolefightingmaster` VALUES ('4195148', '21448', '190', '30', '2017051711', '1', '100', '2005', '67', '慕容停岩', '1', '68', '0', '2017071200', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195152', '112156', '51', '1', '2017063009', '0', '60', '2001', '0', '沐希梧', '1', '120', '0', '2017062810', '');
INSERT INTO `rolefightingmaster` VALUES ('4195156', '34141', '12', '1', '2017042117', '0', '60', '2001', '0', '屈安棠', '1', '42', '0', '2017042117', '');
INSERT INTO `rolefightingmaster` VALUES ('4195157', '158448', '63', '2', '2017071121', '0', '60', '2001', '0', '我与你', '1', '84', '0', '2017070709', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195162', '193406', '557', '14', '2017071114', '0', '40', '1009', '14', '齐玉卿', '1', '76', '0', '2017071200', '912+911');
INSERT INTO `rolefightingmaster` VALUES ('4195164', '88619', '304', '9', '2017042719', '0', '50', '1002', '53', '即墨以岚', '1', '72', '0', '2017071200', '912+911');
INSERT INTO `rolefightingmaster` VALUES ('4195166', '141303', '120', '1', '2017061011', '0', '50', '1001', '0', '洛南河', '1', '40', '0', '2017061011', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195171', '25520', '62', '1', '2017041817', '1', '50', '1001', '0', '荆亚岚', '1', '40', '0', '2017041817', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195173', '161009', '1', '9', '2017070711', '0', '140', '2009', '0', '洛听沙', '1', '82', '0', '2017070711', '');
INSERT INTO `rolefightingmaster` VALUES ('4195174', '89979', '66', '1', '2017071016', '0', '50', '1001', '0', '祝七沙', '1', '100', '0', '2017071016', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195175', '73963', '215', '5', '2017041910', '5', '60', '2001', '52', '林灵石', '1', '58', '1', '2017071200', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195180', '33681', '12', '1', '2017041910', '1', '60', '2001', '0', '梦从末', '1', '42', '0', '2017041910', '');
INSERT INTO `rolefightingmaster` VALUES ('4195181', '33908', '66', '1', '2017042409', '0', '50', '1001', '0', '向天', '1', '41', '0', '2017041914', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195184', '40180', '269', '5', '2017062614', '0', '50', '1005', '14', '柳息', '1', '44', '0', '2017071200', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195185', '39702', '15', '1', '2017070715', '0', '60', '2001', '0', '诸葛镇玺', '1', '42', '0', '2017070715', '');
INSERT INTO `rolefightingmaster` VALUES ('4195186', '33056', '66', '1', '2017041917', '1', '50', '1001', '0', '白遇默', '1', '40', '0', '2017041917', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195188', '46740', '23', '0', '2017062917', '0', '50', '0', '0', '乔南塘', '1', '100', '0', '2017062917', '');
INSERT INTO `rolefightingmaster` VALUES ('4195191', '81827', '267', '6', '2017071110', '0', '50', '1005', '17', '顾入师', '1', '59', '0', '2017071200', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195195', '35198', '117', '2', '2017042015', '2', '50', '1002', '0', '梁思廷', '1', '40', '0', '2017042015', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195196', '34164', '12', '1', '2017060110', '0', '60', '2001', '0', '陈离', '1', '42', '0', '2017060110', '');
INSERT INTO `rolefightingmaster` VALUES ('4195222', '41880', '70', '1', '2017060917', '0', '50', '1001', '0', '凌南', '1', '42', '0', '2017060917', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195235', '34592', '117', '2', '2017062612', '0', '50', '1002', '0', '辛剑生', '1', '41', '0', '2017062415', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195236', '37208', '214', '5', '2017042511', '0', '60', '2001', '53', '独孤晴', '1', '41', '0', '2017071200', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195250', '368438', '184', '0', '2017062721', '0', '50', '0', '69', '姬一', '1', '100', '0', '2017071200', '');
INSERT INTO `rolefightingmaster` VALUES ('4195252', '399280', '277', '12', '2017071115', '0', '60', '2001', '5', 'name', '1', '102', '0', '2017071200', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195253', '123559', '37', '0', '2017042509', '0', '50', '0', '0', '洛全塘', '1', '133', '0', '2017042211', '');
INSERT INTO `rolefightingmaster` VALUES ('4195255', '424215', '463', '15', '2017052610', '0', '40', '1002', '22', '风筱衣', '1', '100', '0', '2017071200', '912+911');
INSERT INTO `rolefightingmaster` VALUES ('4195281', '2626294', '227', '3', '2017071110', '0', '50', '1003', '43', '习习蛤蛤', '1', '130', '0', '2017071200', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195285', '35216', '117', '2', '2017042511', '2', '50', '1002', '0', '叶振月', '1', '41', '0', '2017042511', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195324', '35938', '117', '2', '2017052017', '0', '50', '1002', '0', '叶子画', '1', '41', '0', '2017051815', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195326', '834601', '335', '9', '2017063010', '0', '50', '1001', '41', '纳兰蕴', '1', '78', '0', '2017071200', '912');
INSERT INTO `rolefightingmaster` VALUES ('4195335', '40427', '70', '1', '2017042520', '1', '50', '1001', '0', '风展棋', '1', '40', '0', '2017042520', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195341', '37199', '213', '5', '2017050214', '0', '60', '2001', '56', '施惜', '1', '41', '0', '2017071200', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195342', '34544', '12', '1', '2017042610', '1', '60', '2001', '0', '温归夫', '1', '40', '0', '2017042610', '');
INSERT INTO `rolefightingmaster` VALUES ('4195343', '60177', '117', '2', '2017042709', '0', '50', '1002', '0', '风延月', '1', '54', '0', '2017042614', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195345', '145001', '66', '1', '2017042611', '1', '60', '2001', '0', '李倾塘', '1', '41', '0', '2017042611', '');
INSERT INTO `rolefightingmaster` VALUES ('4195348', '26088', '63', '1', '2017042811', '0', '50', '1001', '0', '姜又轩', '1', '40', '0', '2017042611', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195353', '168459', '134', '1', '2017050310', '0', '50', '1001', '100', '画离依', '1', '40', '0', '2017071200', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195354', '65674', '116', '2', '2017042709', '0', '50', '1002', '0', '万百词', '1', '55', '0', '2017042616', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195355', '33693', '66', '1', '2017042910', '0', '50', '1001', '0', '云仲楼', '1', '41', '0', '2017042616', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195356', '50553', '75', '1', '2017052615', '0', '50', '1001', '0', '职业玩家', '1', '40', '0', '2017052615', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195357', '640096', '351', '1', '2017070310', '0', '40', '1001', '35', '欧阳笛', '1', '100', '0', '2017071200', '912');
INSERT INTO `rolefightingmaster` VALUES ('4195358', '34298', '212', '5', '2017062015', '0', '60', '2001', '58', '帅笔', '1', '41', '0', '2017071200', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195360', '883246', '67', '2', '2017062315', '1', '70', '2002', '0', '桑丘九生', '1', '120', '0', '2017062315', '');
INSERT INTO `rolefightingmaster` VALUES ('4195363', '34168', '67', '1', '2017042811', '0', '50', '1001', '0', '乐正其睿', '1', '41', '0', '2017042717', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195369', '84567', '213', '5', '2017062015', '0', '60', '2001', '55', '宁语陌', '1', '58', '0', '2017071200', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195376', '33558', '66', '1', '2017051009', '0', '50', '1001', '0', '梅心', '1', '41', '0', '2017051009', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195379', '24523', '6', '1', '2017042711', '1', '60', '2001', '0', '闻人令琢', '1', '41', '0', '2017042711', '');
INSERT INTO `rolefightingmaster` VALUES ('4195380', '32797', '266', '5', '2017042714', '5', '50', '1005', '22', '花灵', '1', '41', '0', '2017071200', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195383', '557820', '265', '6', '2017071109', '0', '60', '2001', '23', '方一绝', '1', '87', '0', '2017071200', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195385', '218327', '341', '7', '2017042915', '0', '40', '1001', '38', '司空治裳', '1', '55', '0', '2017071200', '912+911');
INSERT INTO `rolefightingmaster` VALUES ('4195387', '33454', '266', '5', '2017050516', '0', '50', '1005', '21', '江语画', '1', '41', '0', '2017071200', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195388', '390238', '4', '5', '2017070309', '0', '100', '2005', '0', '顾季之', '1', '86', '0', '2017062916', '');
INSERT INTO `rolefightingmaster` VALUES ('4195391', '35292', '67', '1', '2017062414', '0', '50', '1001', '0', '新手流程', '1', '41', '0', '2017062414', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195392', '72027', '161', '2', '2017071118', '0', '50', '1002', '84', '摩卡冰', '1', '55', '0', '2017071200', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195398', '303377', '168', '3', '2017061017', '0', '50', '1003', '79', '吴季玺', '1', '92', '0', '2017071200', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195401', '183625', '213', '5', '2017062811', '0', '60', '2001', '54', '程落啸', '1', '56', '0', '2017071200', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195419', '105641', '97', '2', '2017042809', '0', '60', '2001', '0', '上官剑歌', '1', '40', '0', '2017042719', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195424', '1053007', '621', '31', '2017071109', '0', '55', '2001', '9', '栾若', '1', '104', '0', '2017071200', '912+911');
INSERT INTO `rolefightingmaster` VALUES ('4195440', '275990', '137', '0', '2017062715', '0', '50', '0', '98', '周扬儿', '1', '120', '0', '2017071200', '');
INSERT INTO `rolefightingmaster` VALUES ('4195445', '36331', '113', '3', '2017042911', '3', '50', '1001', '0', '吴思楼', '1', '41', '0', '2017042911', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195447', '35754', '67', '1', '2017050909', '0', '50', '1001', '0', '即墨安景', '1', '41', '0', '2017050909', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195454', '108649', '104', '1', '2017062315', '0', '50', '1001', '0', '洛全画', '1', '40', '0', '2017062315', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195460', '36035', '68', '1', '2017052709', '0', '50', '1001', '0', '蔺敬轩', '1', '41', '0', '2017052623', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195465', '26106', '13', '0', '2017070411', '0', '50', '0', '0', 'tgf601', '1', '62', '0', '2017070411', '');
INSERT INTO `rolefightingmaster` VALUES ('4195467', '175158', '154', '6', '2017050909', '0', '50', '1001', '91', '皇甫扬', '1', '62', '0', '2017071200', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195475', '33947', '66', '1', '2017051717', '0', '50', '1001', '0', '琴牧卿', '1', '41', '0', '2017051717', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195476', '140512', '120', '1', '2017060111', '0', '50', '1001', '0', '必优机都出来', '1', '40', '0', '2017060111', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195478', '33458', '66', '1', '2017050209', '0', '50', '1001', '0', '冷五', '1', '40', '0', '2017043014', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195481', '30540', '65', '1', '2017043014', '1', '50', '1001', '0', '西门永睿', '1', '40', '0', '2017043014', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195482', '40259', '70', '1', '2017043018', '1', '50', '1001', '0', '独孤听仪', '1', '40', '0', '2017043018', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195486', '34145', '11', '1', '2017043015', '1', '60', '2001', '0', '梦思南', '1', '41', '0', '2017043015', '');
INSERT INTO `rolefightingmaster` VALUES ('4195489', '62961', '31', '0', '2017043019', '0', '50', '0', '0', '姬少画', '1', '150', '0', '2017043019', '');
INSERT INTO `rolefightingmaster` VALUES ('4195497', '23319', '16', '12', '2017050211', '0', '160', '2011', '0', '冷飞泉', '1', '46', '0', '2017050211', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195500', '34381', '67', '1', '2017071209', '0', '50', '1001', '0', '端木寄镜', '1', '40', '0', '2017071209', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195502', '2601256', '967', '1', '2017071016', '0', '30', '1001', '6', '磁暴步兵', '1', '130', '0', '2017071200', '913');
INSERT INTO `rolefightingmaster` VALUES ('4195503', '40558', '70', '1', '2017052709', '0', '50', '1001', '0', '第五远轲', '1', '100', '0', '2017052516', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195506', '900763', '469', '18', '2017071110', '0', '70', '2002', '21', '桑可', '1', '95', '0', '2017071200', '912+911');
INSERT INTO `rolefightingmaster` VALUES ('4195507', '34277', '67', '1', '2017050509', '0', '50', '1001', '0', '难度测71', '1', '41', '0', '2017050316', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195508', '38688', '69', '1', '2017052309', '0', '50', '1001', '0', '青阳七夫', '1', '42', '0', '2017052216', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195509', '34082', '67', '1', '2017050318', '1', '50', '1001', '0', '你猜我猜不猜', '1', '41', '0', '2017050318', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195510', '110526', '268', '6', '2017071209', '0', '50', '1003', '15', '左丘落', '1', '56', '0', '2017071200', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195515', '149359', '124', '1', '2017050318', '1', '50', '1001', '0', '曲惜词', '1', '41', '0', '2017052400', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195519', '243527', '341', '7', '2017051010', '0', '40', '1007', '37', '我的号呢', '1', '75', '0', '2017071200', '912+911');
INSERT INTO `rolefightingmaster` VALUES ('4195551', '111232', '55', '0', '2017062010', '0', '50', '0', '0', '沈治镜', '1', '74', '0', '2017061911', '');
INSERT INTO `rolefightingmaster` VALUES ('4195552', '55355', '62', '3', '2017070614', '0', '70', '2002', '0', '蔺天师', '1', '46', '0', '2017070614', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195553', '1691048', '339', '7', '2017070417', '0', '50', '1001', '40', '1丶王师傅', '1', '120', '0', '2017071200', '912+911');
INSERT INTO `rolefightingmaster` VALUES ('4195554', '109126', '104', '1', '2017071110', '0', '50', '1001', '0', '2丶李师傅', '1', '100', '0', '2017071110', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195555', '111308', '105', '1', '2017052614', '0', '50', '1001', '0', '3丶赵师傅', '1', '100', '0', '2017052614', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195556', '108379', '104', '1', '2017052321', '0', '50', '1001', '0', '4丶孙师傅', '1', '100', '0', '2017052014', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195557', '109530', '104', '1', '2017071123', '0', '50', '1001', '0', '5丶钱师傅', '1', '100', '0', '2017071123', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195558', '110079', '105', '1', '2017062414', '0', '50', '1001', '0', '6丶何师傅', '1', '100', '0', '2017062414', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195559', '110863', '105', '1', '2017071123', '0', '50', '1001', '0', '7丶吕师傅', '1', '100', '0', '2017071123', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195561', '35055', '67', '1', '2017070614', '0', '50', '1001', '0', '平宁听堂', '1', '41', '0', '2017070614', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195562', '34506', '67', '1', '2017052620', '0', '50', '1001', '0', '施少啸', '1', '41', '0', '2017052620', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195564', '99641', '102', '3', '2017061409', '0', '50', '1001', '0', '鱼墨', '1', '29', '0', '2017061409', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195565', '26715', '63', '1', '2017050510', '1', '50', '1001', '0', '独孤又桂', '1', '40', '0', '2017050510', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195567', '132508', '116', '1', '2017061013', '0', '50', '1001', '0', '周右凉', '1', '40', '0', '2017061013', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195581', '34140', '67', '1', '2017051210', '0', '50', '1001', '0', '难度测74', '1', '41', '0', '2017051210', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195582', '400466', '261', '6', '2017061019', '0', '60', '2001', '26', 'ta2', '1', '74', '0', '2017071200', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195586', '37776', '163', '4', '2017071111', '0', '50', '1001', '82', '曲其澜', '1', '41', '0', '2017071200', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195594', '615484', '317', '15', '2017071209', '0', '100', '2004', '47', '12345', '1', '83', '0', '2017071200', '912+911');
INSERT INTO `rolefightingmaster` VALUES ('4195603', '50725', '75', '1', '2017052618', '0', '50', '1001', '0', '夙逸恭', '1', '40', '0', '2017052618', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195605', '34664', '11', '1', '2017051018', '1', '60', '2001', '0', '白羽', '1', '40', '0', '2017051018', '');
INSERT INTO `rolefightingmaster` VALUES ('4195611', '969276', '221', '5', '2017071109', '0', '50', '1002', '48', '百里知石', '1', '120', '0', '2017071200', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195620', '48649', '46', '3', '2017070614', '0', '70', '2002', '0', '万振', '1', '41', '0', '2017070614', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195636', '49308', '74', '1', '2017071123', '0', '50', '1001', '0', '8丶吴师傅', '1', '40', '0', '2017071123', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195637', '46009', '73', '1', '2017061314', '0', '50', '1001', '0', '9丶郑师傅', '1', '100', '0', '2017060817', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195638', '2337018', '73', '1', '2017061911', '0', '50', '1001', '0', '10丶周师傅', '1', '120', '0', '2017061413', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195641', '68183', '84', '1', '2017060116', '0', '50', '1001', '0', '秦展生', '1', '40', '0', '2017060116', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195664', '310860', '155', '0', '2017051620', '0', '50', '0', '88', '端木振', '1', '150', '0', '2017071200', '');
INSERT INTO `rolefightingmaster` VALUES ('4195670', '241695', '300', '4', '2017051914', '0', '50', '1004', '55', '甄全睿', '1', '99', '0', '2017071200', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195676', '35259', '67', '1', '2017061918', '0', '50', '1001', '0', '曲牧岩', '1', '41', '0', '2017061918', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195685', '14444', '1', '1', '2017060511', '1', '60', '2001', '0', '年晓桂', '1', '26', '0', '2017060511', '');
INSERT INTO `rolefightingmaster` VALUES ('4195686', '56181', '116', '2', '2017052310', '2', '50', '1002', '0', '欧阳入', '1', '52', '0', '2017052310', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195695', '13412', '56', '1', '2017062009', '0', '50', '1001', '0', '阮晴石', '1', '23', '0', '2017062009', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195699', '36597', '103', '5', '2017071117', '0', '50', '1001', '0', '冷亚石', '1', '41', '0', '2017070514', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195701', '35323', '67', '1', '2017052316', '1', '50', '1001', '0', '夏听蓝', '1', '40', '0', '2017052316', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195704', '37426', '68', '1', '2017060917', '0', '50', '1001', '0', '曲于景', '1', '41', '0', '2017060917', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195709', '34744', '67', '1', '2017052511', '0', '50', '1001', '0', '桑于英', '1', '40', '0', '2017052511', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195714', '32669', '66', '1', '2017061317', '0', '50', '1001', '0', '鱼晴真', '1', '40', '0', '2017061210', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195717', '68438', '64', '1', '2017052514', '0', '50', '1001', '0', '温剑河', '1', '53', '0', '2017052414', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195718', '116994', '549', '12', '2017052417', '0', '50', '1012', '15', '芈百', '1', '66', '0', '2017071200', '912+911');
INSERT INTO `rolefightingmaster` VALUES ('4195719', '26901', '58', '2', '2017052420', '2', '50', '1001', '0', '杜亦城', '1', '40', '0', '2017052420', '');
INSERT INTO `rolefightingmaster` VALUES ('4195720', '35136', '12', '1', '2017052511', '1', '60', '2001', '0', '青阳九绝', '1', '40', '0', '2017052511', '');
INSERT INTO `rolefightingmaster` VALUES ('4195721', '119927', '305', '11', '2017061409', '0', '55', '2001', '52', '想清楚', '1', '63', '0', '2017071200', '912+911');
INSERT INTO `rolefightingmaster` VALUES ('4195722', '25777', '62', '1', '2017060211', '1', '50', '1001', '0', '蔺安衣', '1', '37', '0', '2017060211', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195723', '47245', '68', '2', '2017052712', '0', '60', '2001', '0', '阮扬紫', '1', '40', '0', '2017052610', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195761', '31574', '149', '5', '2017052619', '5', '50', '1002', '94', '平宁未卿', '1', '32', '1', '2017071200', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195764', '1045409', '51', '2', '2017071111', '0', '60', '2001', '0', '难度测88', '1', '121', '0', '2017070611', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195766', '13368', '56', '1', '2017052620', '1', '50', '1001', '0', '凌若逐', '1', '23', '0', '2017052620', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195767', '13355', '56', '1', '2017052621', '1', '50', '1001', '0', '夏叔为', '1', '23', '0', '2017052621', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195768', '13387', '196', '6', '2017071119', '0', '60', '2001', '65', '方永紫', '1', '23', '0', '2017071200', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195769', '31584', '10', '1', '2017071109', '0', '60', '2001', '0', '夙相真', '1', '104', '0', '2017071109', '');
INSERT INTO `rolefightingmaster` VALUES ('4195770', '13372', '56', '1', '2017052710', '1', '50', '1001', '0', '难度测89', '1', '23', '0', '2017052710', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195776', '18436', '151', '4', '2017062721', '3', '50', '1001', '93', '栾长如', '1', '29', '0', '2017071200', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195777', '13362', '56', '1', '2017061609', '0', '50', '1001', '0', '杨晓隐', '1', '23', '0', '2017061609', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195778', '30682', '91', '5', '2017071016', '0', '80', '2003', '0', '左丘敬庭', '1', '34', '0', '2017071016', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195779', '791137', '50', '3', '2017071209', '0', '70', '2002', '0', '秦离城', '1', '102', '0', '2017071209', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195780', '13389', '56', '1', '2017070319', '0', '50', '1001', '0', '青阳扬烟', '1', '23', '0', '2017070319', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195782', '14450', '1', '1', '2017060815', '0', '60', '2001', '0', '施若末', '1', '26', '0', '2017060815', '');
INSERT INTO `rolefightingmaster` VALUES ('4195784', '144384', '122', '1', '2017060515', '0', '50', '1001', '0', '池可鸾', '1', '23', '0', '2017060111', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195785', '19625', '9', '0', '2017053117', '0', '50', '0', '0', '君丹', '1', '31', '0', '2017053117', '');
INSERT INTO `rolefightingmaster` VALUES ('4195788', '14102', '2', '1', '2017070309', '0', '60', '2001', '0', '左惜襄', '1', '25', '0', '2017063018', '');
INSERT INTO `rolefightingmaster` VALUES ('4195794', '13333', '1', '1', '2017053116', '1', '60', '2001', '0', '左少', '1', '23', '0', '2017053116', '');
INSERT INTO `rolefightingmaster` VALUES ('4195796', '13335', '91', '5', '2017060218', '5', '70', '2002', '0', '芈碧', '1', '23', '0', '2017060218', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195800', '126365', '113', '1', '2017053118', '1', '50', '1001', '0', '宋一殊', '1', '23', '0', '2017053118', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195803', '13356', '56', '1', '2017053118', '1', '50', '1001', '0', '乔振鸾', '1', '23', '0', '2017053118', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195805', '32667', '6', '2', '2017060109', '0', '70', '2002', '0', '花亦言', '1', '105', '0', '2017053118', '');
INSERT INTO `rolefightingmaster` VALUES ('4195808', '13387', '56', '1', '2017060109', '1', '50', '1001', '0', '鹿治之', '1', '23', '0', '2017060109', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195811', '13400', '56', '1', '2017060110', '1', '50', '1001', '0', '顾与英', '1', '23', '0', '2017060110', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195815', '168425', '134', '1', '2017061919', '0', '50', '1001', '0', '芈觅月', '1', '23', '0', '2017062100', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195817', '13328', '56', '1', '2017060116', '1', '50', '1001', '0', '蔺一恭', '1', '23', '0', '2017060116', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195829', '13369', '1', '1', '2017062009', '0', '60', '2001', '0', '方南松', '1', '23', '0', '2017061918', '');
INSERT INTO `rolefightingmaster` VALUES ('4195830', '13392', '56', '1', '2017071121', '0', '50', '1001', '0', '左停之', '1', '23', '0', '2017071121', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195831', '46159', '158', '6', '2017060215', '0', '60', '2001', '86', '司空逸南', '1', '80', '0', '2017071200', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195832', '27938', '60', '1', '2017070715', '0', '50', '1001', '0', '君磬琢', '1', '31', '0', '2017070715', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195833', '252135', '199', '7', '2017060216', '0', '50', '1002', '63', '杨可', '1', '82', '0', '2017071200', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195834', '13394', '56', '1', '2017060509', '1', '50', '1001', '0', '赵令桂', '1', '23', '0', '2017060509', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195835', '45671', '22', '0', '2017060216', '0', '50', '0', '0', '宋百亭', '1', '96', '0', '2017060216', '');
INSERT INTO `rolefightingmaster` VALUES ('4195836', '291011', '142', '0', '2017060216', '0', '50', '0', '95', '年袭石', '1', '119', '0', '2017071200', '');
INSERT INTO `rolefightingmaster` VALUES ('4195838', '26247', '63', '1', '2017060217', '1', '50', '1001', '0', '莫全山', '1', '23', '0', '2017060217', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195839', '13397', '56', '1', '2017060716', '0', '50', '1001', '0', '洛与山', '1', '23', '0', '2017060716', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195840', '21466', '55', '2', '2017060509', '0', '60', '2001', '0', '易过歌', '1', '23', '0', '2017060218', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195841', '13409', '56', '1', '2017060618', '0', '50', '1001', '0', '百里右', '1', '23', '0', '2017060509', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195844', '109262', '49', '1', '2017071110', '1', '60', '2001', '0', '百里子', '1', '23', '0', '2017071110', '');
INSERT INTO `rolefightingmaster` VALUES ('4195848', '23424', '51', '2', '2017071115', '0', '60', '2001', '0', '凤息睿', '1', '31', '0', '2017071011', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195849', '13371', '101', '3', '2017062009', '0', '60', '2001', '0', '梁相', '1', '23', '0', '2017061918', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195850', '13359', '56', '1', '2017060514', '1', '50', '1001', '0', '萧吟竹', '1', '23', '0', '2017060514', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195853', '13412', '56', '1', '2017061311', '0', '50', '1001', '0', '栾千凉', '1', '23', '0', '2017061311', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195856', '13377', '56', '1', '2017071114', '0', '50', '1001', '0', '沐子沙', '1', '23', '0', '2017070514', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195857', '38204', '1', '4', '2017071109', '0', '90', '2004', '0', '易敬雨', '1', '117', '0', '2017070611', '');
INSERT INTO `rolefightingmaster` VALUES ('4195860', '13336', '1', '1', '2017070318', '0', '60', '2001', '0', '桑亦儿', '1', '23', '0', '2017070318', '');
INSERT INTO `rolefightingmaster` VALUES ('4195861', '36163', '1', '2', '2017061409', '1', '70', '2002', '0', '芈过英', '1', '31', '0', '2017061409', '');
INSERT INTO `rolefightingmaster` VALUES ('4195864', '13403', '56', '1', '2017070614', '0', '50', '1001', '0', '秋微兰', '1', '23', '0', '2017070614', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195866', '596256', '224', '2', '2017061310', '0', '50', '1002', '46', '淳公主', '1', '120', '0', '2017071200', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195875', '238056', '169', '1', '2017071122', '0', '50', '1001', '77', '齐以末', '1', '120', '0', '2017071200', '');
INSERT INTO `rolefightingmaster` VALUES ('4195876', '29695', '64', '1', '2017061610', '0', '50', '1001', '0', '楚惜山', '1', '24', '0', '2017061610', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195877', '13431', '56', '1', '2017060910', '1', '50', '1001', '0', '秦心景', '1', '23', '0', '2017060910', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195878', '144352', '122', '1', '2017060914', '1', '50', '1001', '0', '祝小画', '1', '23', '0', '2017060914', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195879', '13334', '56', '1', '2017060915', '1', '50', '1001', '0', '彦辞轩', '1', '23', '0', '2017060915', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195886', '13373', '56', '1', '2017061011', '1', '50', '1001', '0', '韩辞画', '1', '23', '0', '2017061011', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195890', '13372', '56', '1', '2017062617', '0', '50', '1001', '0', '乔长', '1', '23', '0', '2017062218', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195891', '13371', '56', '1', '2017070315', '0', '50', '1001', '0', '姬星', '1', '23', '0', '2017070315', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195893', '13368', '56', '1', '2017061914', '0', '50', '1001', '0', '万俟微袖', '1', '23', '0', '2017061914', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195904', '267856', '236', '9', '2017061409', '3', '60', '2001', '39', '云一', '1', '70', '0', '2017071200', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195905', '120491', '201', '5', '2017061309', '5', '50', '1002', '62', '柳希玺', '1', '64', '1', '2017071200', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195907', '13371', '56', '1', '2017061915', '0', '50', '1001', '0', '花遇', '1', '23', '0', '2017061915', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195908', '13394', '82', '2', '2017061310', '1', '60', '2001', '0', '楚五山', '1', '23', '0', '2017061310', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195909', '21495', '60', '1', '2017061310', '1', '50', '1001', '0', '小小小镜子', '1', '23', '0', '2017061310', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195911', '26281', '101', '3', '2017062009', '0', '60', '2001', '0', '风未', '1', '32', '0', '2017061915', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195912', '13394', '51', '2', '2017061410', '0', '60', '2001', '0', '11丶萝小莉', '1', '23', '0', '2017061410', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195921', '13406', '1', '1', '2017062009', '0', '60', '2001', '0', '姒可', '1', '23', '0', '2017061409', '');
INSERT INTO `rolefightingmaster` VALUES ('4195923', '22410', '95', '5', '2017061318', '5', '70', '2002', '0', '鬼谷南廷', '1', '24', '1', '2017061318', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195924', '13667', '56', '1', '2017061318', '1', '50', '1001', '0', '你全家都萝莉', '1', '24', '0', '2017061318', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195925', '216141', '101', '3', '2017071116', '0', '60', '2001', '0', '姒剑', '1', '120', '0', '2017071116', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195929', '55380', '62', '4', '2017061409', '4', '80', '2003', '0', '鱼宝阁', '1', '23', '0', '2017061409', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195935', '13343', '56', '1', '2017070614', '1', '50', '1001', '0', '秦蕴阁', '1', '23', '0', '2017070614', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195940', '21376', '60', '1', '2017062311', '0', '50', '1001', '0', '秋听', '1', '23', '0', '2017062311', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195941', '13370', '56', '1', '2017070315', '0', '50', '1001', '0', '端木轻英', '1', '23', '0', '2017070315', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195945', '454604', '277', '1', '2017062014', '0', '50', '1001', '4', '喳喳', '1', '115', '0', '2017071200', '');
INSERT INTO `rolefightingmaster` VALUES ('4195947', '13364', '51', '2', '2017062109', '0', '60', '2001', '0', '百里蕴棋', '1', '23', '0', '2017062109', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195959', '227871', '383', '6', '2017062414', '0', '50', '1006', '30', '你使笔吗', '1', '104', '0', '2017071200', '912+911');
INSERT INTO `rolefightingmaster` VALUES ('4195960', '13356', '56', '1', '2017071116', '0', '50', '1001', '0', '谢玉流', '1', '23', '0', '2017071116', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195961', '159441', '341', '10', '2017071109', '0', '50', '1002', '39', '辛怡', '1', '78', '0', '2017071200', '912+911');
INSERT INTO `rolefightingmaster` VALUES ('4195963', '254677', '46', '7', '2017062711', '7', '60', '2001', '0', '符文', '1', '53', '0', '2017062711', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195964', '13356', '56', '1', '2017062716', '1', '50', '1001', '0', '洛小楼', '1', '23', '0', '2017062716', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195965', '126411', '63', '0', '2017071015', '0', '50', '0', '0', '温千尝', '1', '23', '0', '2017071015', '');
INSERT INTO `rolefightingmaster` VALUES ('4195980', '14705', '57', '1', '2017071010', '1', '50', '1001', '0', '宋语睿', '1', '23', '0', '2017071010', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195984', '14716', '2', '1', '2017071116', '0', '60', '2001', '0', '万俟落阁', '1', '23', '0', '2017071020', '');
INSERT INTO `rolefightingmaster` VALUES ('4195988', '101949', '57', '1', '2017071118', '1', '50', '1001', '0', '上官知月', '1', '72', '0', '2017071118', '911');
INSERT INTO `rolefightingmaster` VALUES ('4195994', '426410', '313', '2', '2017022018', '2', '50', '1002', '49', '万怡戏', '1', '150', '0', '2017071200', '911');
INSERT INTO `rolefightingmaster` VALUES ('4196073', '26244', '59', '2', '2017032209', '0', '50', '1001', '0', '顾西为', '1', '56', '0', '2017032209', '');
INSERT INTO `rolefightingmaster` VALUES ('4196088', '46436', '23', '0', '2017020516', '0', '50', '0', '0', '万俟青堂', '1', '146', '0', '2017022200', '');
INSERT INTO `rolefightingmaster` VALUES ('4196095', '35608', '67', '1', '2017020619', '1', '50', '1001', '0', '乐正辞梨', '1', '112', '0', '2017022200', '');
INSERT INTO `rolefightingmaster` VALUES ('4196155', '25134', '12', '0', '2017022211', '0', '50', '0', '0', '吴逸歌', '1', '54', '0', '2017022211', '');
INSERT INTO `rolefightingmaster` VALUES ('4196194', '64547', '82', '1', '2017020809', '1', '50', '1001', '0', '端木仲树', '1', '55', '0', '2017022200', '');
INSERT INTO `rolefightingmaster` VALUES ('4196195', '83975', '181', '3', '2017020916', '3', '50', '1003', '74', '梦怡苏', '1', '58', '0', '2017071200', '911');
INSERT INTO `rolefightingmaster` VALUES ('4196200', '926309', '593', '0', '2017032009', '0', '50', '0', '10', '公共服最高战', '1', '150', '0', '2017071200', '');
INSERT INTO `rolefightingmaster` VALUES ('4196203', '196302', '70', '7', '2017032116', '7', '120', '2007', '0', '冷玉兰', '1', '95', '0', '2017032116', '');
INSERT INTO `rolefightingmaster` VALUES ('4196208', '223248', '182', '4', '2017032109', '0', '60', '2001', '71', '上官星', '1', '85', '0', '2017071200', '911');
INSERT INTO `rolefightingmaster` VALUES ('4196220', '2107568', '1194', '7', '2017031810', '0', '50', '1007', '3', '特朗普', '1', '150', '0', '2017071200', '913');
INSERT INTO `rolefightingmaster` VALUES ('4196279', '167184', '300', '15', '2017031715', '0', '50', '1001', '56', '肉夹馍', '1', '76', '0', '2017071200', '912+911');
INSERT INTO `rolefightingmaster` VALUES ('4196285', '2376982', '655', '13', '2017032211', '0', '50', '1008', '8', '左天戏', '1', '150', '0', '2017071200', '912+911');
INSERT INTO `rolefightingmaster` VALUES ('4196299', '681638', '152', '15', '2017032209', '0', '100', '2005', '92', '万宜南', '1', '150', '0', '2017071200', '911');
INSERT INTO `rolefightingmaster` VALUES ('4196300', '158276', '79', '0', '2017032115', '0', '50', '0', '0', '梦治苏', '1', '149', '0', '2017031509', '');
INSERT INTO `rolefightingmaster` VALUES ('4196349', '421864', '822', '24', '2017032211', '0', '60', '2001', '8', '凤锐玺', '1', '107', '0', '2017071200', '912+913+911');
INSERT INTO `rolefightingmaster` VALUES ('4196350', '144012', '111', '5', '2017032020', '0', '50', '1001', '0', '万遇', '1', '145', '0', '2017031510', '911');
INSERT INTO `rolefightingmaster` VALUES ('4196352', '558262', '136', '3', '2017032014', '0', '50', '1001', '99', '祝亚琢', '1', '150', '0', '2017071200', '911');
INSERT INTO `rolefightingmaster` VALUES ('4196375', '166612', '83', '0', '2017031711', '0', '50', '0', '0', '祝亦轩', '1', '150', '0', '2017031514', '');
INSERT INTO `rolefightingmaster` VALUES ('4196394', '79837', '89', '1', '2017021410', '1', '50', '1001', '0', '程细萝', '1', '125', '0', '2017030218', '');
INSERT INTO `rolefightingmaster` VALUES ('4196404', '91796', '142', '2', '2017032118', '1', '50', '1002', '96', '蔺筱之', '1', '64', '0', '2017071200', '911');
INSERT INTO `rolefightingmaster` VALUES ('4196423', '46631', '23', '0', '2017021508', '0', '50', '0', '0', '柳亚夫', '1', '73', '0', '2017022200', '');
INSERT INTO `rolefightingmaster` VALUES ('4196435', '47269', '73', '1', '2017021616', '1', '50', '1001', '0', '白落襄', '1', '43', '0', '2017022200', '');
INSERT INTO `rolefightingmaster` VALUES ('4196444', '28340', '14', '0', '2017031711', '0', '50', '0', '0', '冷怡', '1', '60', '0', '2017031621', '');
INSERT INTO `rolefightingmaster` VALUES ('4196449', '66755', '25', '2', '2017031715', '0', '70', '2002', '0', '祝晴', '1', '150', '0', '2017031511', '');
INSERT INTO `rolefightingmaster` VALUES ('4196456', '115997', '21', '0', '2017030610', '0', '50', '0', '0', '上官右', '1', '132', '0', '2017030116', '');
INSERT INTO `rolefightingmaster` VALUES ('4196464', '16400', '8', '0', '2017021715', '0', '50', '0', '0', '独孤晴堂', '1', '52', '0', '2017022200', '');
INSERT INTO `rolefightingmaster` VALUES ('4196467', '120343', '85', '2', '2017031711', '0', '50', '1001', '0', '阮晓萝', '1', '84', '0', '2017031711', '911');
INSERT INTO `rolefightingmaster` VALUES ('4196492', '139107', '255', '4', '2017032210', '0', '50', '1004', '28', 'zej1', '1', '67', '0', '2017071200', '911');
INSERT INTO `rolefightingmaster` VALUES ('4196501', '82547', '91', '1', '2017022020', '1', '50', '1001', '0', '闻书亭', '1', '78', '0', '2017022200', '');
INSERT INTO `rolefightingmaster` VALUES ('4196575', '69726', '84', '1', '2017031620', '1', '50', '1001', '0', '年轻如', '1', '59', '0', '2017031620', '');
INSERT INTO `rolefightingmaster` VALUES ('4196581', '74190', '87', '1', '2017030615', '1', '50', '1001', '0', '宁于卿', '1', '57', '0', '2017031011', '');
INSERT INTO `rolefightingmaster` VALUES ('4196588', '31550', '111', '3', '2017032110', '0', '50', '1002', '0', '姜遇', '1', '85', '0', '2017032014', '911');
INSERT INTO `rolefightingmaster` VALUES ('4196616', '388607', '30', '2', '2017031814', '0', '70', '2002', '0', '凌青鸾', '1', '121', '0', '2017031514', '');
INSERT INTO `rolefightingmaster` VALUES ('4196622', '210408', '74', '2', '2017032009', '0', '60', '2001', '0', '顾仲南', '1', '74', '0', '2017031711', '911');
INSERT INTO `rolefightingmaster` VALUES ('4196625', '151903', '265', '12', '2017032211', '0', '50', '1002', '24', '江瑞镜', '1', '74', '0', '2017071200', '911');
INSERT INTO `rolefightingmaster` VALUES ('4196645', '399423', '219', '2', '2017032009', '1', '70', '2002', '51', '欧阳凡凡', '1', '150', '0', '2017071200', '');
INSERT INTO `rolefightingmaster` VALUES ('4196652', '53829', '26', '0', '2017022221', '0', '50', '0', '0', '莫青树', '1', '56', '0', '2017030914', '');
INSERT INTO `rolefightingmaster` VALUES ('4196778', '56692', '224', '5', '2017022511', '5', '60', '2001', '47', '池知', '1', '150', '0', '2017071200', '911');
INSERT INTO `rolefightingmaster` VALUES ('4196779', '46508', '23', '0', '2017022511', '0', '50', '0', '0', '夙五鸾', '1', '150', '0', '2017022511', '');
INSERT INTO `rolefightingmaster` VALUES ('4196800', '51789', '221', '5', '2017032115', '0', '50', '1002', '49', '秋展南', '1', '56', '0', '2017071200', '911');
INSERT INTO `rolefightingmaster` VALUES ('4196805', '27312', '63', '1', '2017032209', '0', '50', '1001', '0', '鱼天生', '1', '87', '0', '2017032209', '911');
INSERT INTO `rolefightingmaster` VALUES ('4196812', '93826', '242', '5', '2017031617', '0', '60', '2001', '35', '姬迟霜', '1', '130', '0', '2017071200', '911');
INSERT INTO `rolefightingmaster` VALUES ('4196822', '63179', '31', '0', '2017032210', '0', '50', '0', '0', '皇甫惊楠', '1', '54', '0', '2017032210', '');
INSERT INTO `rolefightingmaster` VALUES ('4196860', '2850496', '1425', '0', '2017032114', '0', '50', '0', '1', '天下第一', '1', '150', '0', '2017071200', '');
INSERT INTO `rolefightingmaster` VALUES ('4196868', '57851', '1', '8', '2017032017', '0', '130', '2008', '0', '温微', '1', '150', '0', '2017031509', '');
INSERT INTO `rolefightingmaster` VALUES ('4196876', '94543', '47', '0', '2017030214', '0', '50', '0', '0', '平宁右师', '1', '130', '0', '2017030214', '');
INSERT INTO `rolefightingmaster` VALUES ('4196881', '14176', '56', '1', '2017031810', '0', '50', '1001', '0', '乐正七师', '1', '46', '0', '2017031810', '911');
INSERT INTO `rolefightingmaster` VALUES ('4196897', '49765', '74', '1', '2017030309', '1', '50', '1001', '0', '云千之', '1', '50', '0', '2017030309', '');
INSERT INTO `rolefightingmaster` VALUES ('4196903', '62758', '17', '4', '2017032211', '0', '90', '2004', '0', '玉罗刹', '1', '61', '0', '2017032211', '');
INSERT INTO `rolefightingmaster` VALUES ('4196908', '15092', '1', '2', '2017031710', '0', '70', '2002', '0', '林飞', '1', '32', '0', '2017031621', '');
INSERT INTO `rolefightingmaster` VALUES ('4196928', '50628', '413', '26', '2017030610', '26', '50', '1002', '24', '斗神殿1', '1', '50', '1', '2017071200', '912+911');
INSERT INTO `rolefightingmaster` VALUES ('4196929', '143488', '540', '14', '2017032209', '0', '50', '1003', '16', '谢碧鸾', '1', '63', '0', '2017071200', '912+911');
INSERT INTO `rolefightingmaster` VALUES ('4196937', '72383', '86', '1', '2017030810', '1', '50', '1001', '0', '莫听', '1', '58', '0', '2017030810', '');
INSERT INTO `rolefightingmaster` VALUES ('4196940', '44282', '392', '9', '2017030611', '9', '60', '2001', '28', '萧息明', '1', '57', '0', '2017071200', '912+911');
INSERT INTO `rolefightingmaster` VALUES ('4196944', '35453', '17', '0', '2017031710', '0', '50', '0', '0', '司空六堂', '1', '64', '0', '2017031517', '');
INSERT INTO `rolefightingmaster` VALUES ('4196945', '603111', '349', '1', '2017032209', '0', '50', '1001', '36', '君一塘', '1', '150', '0', '2017071200', '912');
INSERT INTO `rolefightingmaster` VALUES ('4196947', '69274', '84', '1', '2017032109', '0', '50', '1001', '0', '宋又生', '1', '130', '0', '2017032016', '');
INSERT INTO `rolefightingmaster` VALUES ('4196950', '50013', '390', '9', '2017030617', '9', '50', '1004', '29', '齐一兮', '1', '51', '0', '2017071200', '912+911');
INSERT INTO `rolefightingmaster` VALUES ('4196954', '65733', '693', '21', '2017031615', '0', '50', '1001', '7', '荆于', '1', '50', '0', '2017071200', '912+911');
INSERT INTO `rolefightingmaster` VALUES ('4196959', '488088', '220', '5', '2017032117', '0', '60', '2001', '50', '柳吟', '1', '150', '0', '2017071200', '911');
INSERT INTO `rolefightingmaster` VALUES ('4196965', '419616', '141', '2', '2017031716', '0', '50', '1002', '97', '纳兰相流', '1', '94', '0', '2017071200', '911');
INSERT INTO `rolefightingmaster` VALUES ('4197045', '679977', '513', '5', '2017032214', '0', '50', '1005', '19', '最多六个中文', '1', '145', '0', '2017071200', '912');
INSERT INTO `rolefightingmaster` VALUES ('4197142', '40221', '20', '0', '2017031715', '0', '50', '0', '0', '即墨过河', '1', '130', '0', '2017031518', '');
INSERT INTO `rolefightingmaster` VALUES ('4197152', '73142', '86', '1', '2017032108', '0', '50', '1001', '0', '齐从词', '1', '56', '0', '2017031510', '');
INSERT INTO `rolefightingmaster` VALUES ('4197171', '211963', '105', '0', '2017031718', '0', '50', '0', '0', '竞技号001', '1', '100', '0', '2017031718', '');
INSERT INTO `rolefightingmaster` VALUES ('4197178', '54504', '77', '1', '2017031318', '1', '50', '1001', '0', '左丘镇景', '1', '55', '0', '2017031318', '');
INSERT INTO `rolefightingmaster` VALUES ('4197191', '118268', '55', '1', '2017031320', '1', '60', '2001', '0', '小笨笨', '1', '56', '0', '2017031509', '');
INSERT INTO `rolefightingmaster` VALUES ('4197193', '293801', '196', '1', '2017032211', '0', '50', '1001', '64', '秦灵隐', '1', '150', '0', '2017071200', '');
INSERT INTO `rolefightingmaster` VALUES ('4197238', '51020', '124', '2', '2017031417', '2', '50', '1002', '0', '东方墨', '1', '150', '0', '2017052400', '911');
INSERT INTO `rolefightingmaster` VALUES ('4197240', '330212', '299', '5', '2017032211', '0', '50', '1005', '1', '君过英', '1', '81', '0', '2017071200', '911');
INSERT INTO `rolefightingmaster` VALUES ('4197241', '83280', '91', '1', '2017031418', '1', '50', '1001', '0', '完胜1', '1', '55', '0', '2017031509', '');
INSERT INTO `rolefightingmaster` VALUES ('4197245', '31288', '1', '5', '2017032018', '5', '100', '2005', '0', '白过兮', '1', '81', '0', '2017032018', '');
INSERT INTO `rolefightingmaster` VALUES ('4197347', '58963', '79', '1', '2017031519', '1', '50', '1001', '0', '荆星为', '1', '72', '0', '2017031519', '');
INSERT INTO `rolefightingmaster` VALUES ('4197348', '557947', '278', '0', '2017031709', '0', '50', '0', '3', '宁其仪', '1', '114', '0', '2017071200', '');
INSERT INTO `rolefightingmaster` VALUES ('4197362', '97203', '98', '1', '2017031809', '0', '50', '1001', '0', '诸葛长河', '1', '58', '0', '2017031715', '');
INSERT INTO `rolefightingmaster` VALUES ('4197442', '121962', '110', '1', '2017031717', '0', '50', '1001', '0', '万其岚', '1', '66', '0', '2017031618', '');
INSERT INTO `rolefightingmaster` VALUES ('4197443', '69182', '30', '1', '2017031618', '1', '60', '2001', '0', '宁寄河', '1', '72', '0', '2017031618', '');
INSERT INTO `rolefightingmaster` VALUES ('4197464', '74056', '37', '0', '2017032009', '0', '50', '0', '0', '风镇轲', '1', '131', '0', '2017031811', '');
INSERT INTO `rolefightingmaster` VALUES ('4197465', '366880', '183', '0', '2017032115', '0', '50', '0', '70', '白南袖', '1', '120', '0', '2017071200', '');
INSERT INTO `rolefightingmaster` VALUES ('4197480', '85941', '92', '1', '2017032210', '0', '50', '1001', '0', '无赦', '1', '58', '0', '2017032210', '');
INSERT INTO `rolefightingmaster` VALUES ('4197488', '90960', '86', '1', '2017031710', '1', '50', '1001', '0', '沐玉', '1', '60', '0', '2017031710', '911');
INSERT INTO `rolefightingmaster` VALUES ('4197506', '48399', '74', '1', '2017031714', '1', '50', '1001', '0', '第五其', '1', '72', '0', '2017031714', '');
INSERT INTO `rolefightingmaster` VALUES ('4197518', '113525', '407', '8', '2017032014', '8', '50', '1008', '25', '青丘白浅上神', '1', '83', '1', '2017071200', '912+911');
INSERT INTO `rolefightingmaster` VALUES ('4197542', '63582', '81', '1', '2017031715', '1', '50', '1001', '0', '赵灵枫', '1', '113', '0', '2017031715', '');
INSERT INTO `rolefightingmaster` VALUES ('4197548', '65107', '74', '1', '2017032211', '0', '50', '1001', '0', '慕容遇屏', '1', '70', '0', '2017032211', '911');
INSERT INTO `rolefightingmaster` VALUES ('4197562', '50240', '75', '1', '2017032017', '1', '50', '1001', '0', '白子南', '1', '51', '0', '2017032017', '');
INSERT INTO `rolefightingmaster` VALUES ('369099469', '25559', '1', '32667', '2017021016', '32667', '326720', '2667', '0', '司空从梨', '1', '52', '0', '2017022200', '');
INSERT INTO `rolefightingmaster` VALUES ('369099473', '131274', '65', '0', '2017031710', '0', '50', '0', '0', '端木可莺', '1', '80', '0', '2017031620', '');
INSERT INTO `rolefightingmaster` VALUES ('369099498', '163770', '244', '4', '2017031814', '0', '50', '1004', '31', '沐扬侠', '1', '91', '0', '2017071200', '911');
INSERT INTO `rolefightingmaster` VALUES ('369099586', '380487', '236', '2', '2017032010', '0', '50', '1001', '37', '乐正西', '1', '149', '0', '2017071200', '');

-- ----------------------------
-- Table structure for `roleinvite`
-- ----------------------------
DROP TABLE IF EXISTS `roleinvite`;
CREATE TABLE `roleinvite` (
  `roleid` bigint(20) NOT NULL COMMENT '角色ID',
  `invitecode` varchar(255) character set utf8 collate utf8_bin NOT NULL COMMENT '邀请码',
  `invitecount` int(255) NOT NULL COMMENT '累计邀请好友个数',
  `fetchcount` int(255) NOT NULL COMMENT '累计领取奖励次数',
  `serverid` int(11) NOT NULL COMMENT '角色所在的服务器ID',
  PRIMARY KEY  (`roleid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of roleinvite
-- ----------------------------
INSERT INTO `roleinvite` VALUES ('4194305', '112427ze', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4194308', '11326im8', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4194310', '111353zq', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4194313', '11204gHj', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4194315', '11203qdZ', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4194318', '11288ig6', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4194321', '11143HUX', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4194324', '11207l6M', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4194325', '11343ChL', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4194328', '11298LOa', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4194329', '11140iZz', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4194330', '11217UoG', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4194331', '11142CM9', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4194334', '11102obj', '1', '1', '1');
INSERT INTO `roleinvite` VALUES ('4194343', '11122eci', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4194344', '11163w1z', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4194346', '11261aFO', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4194348', '11339DPI', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4194350', '11193eMk', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4194351', '1183bo5', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4194356', '11213qHS', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4194357', '1186g05', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4194358', '11220Q6H', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4194359', '11145yQ6', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4194363', '11321l7L', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4194368', '11360Sv2', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4194369', '11202W5h', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4194383', '11214tD4', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4194388', '11265GGZ', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4194392', '11187Izv', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4194399', '11141ZSy', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4194408', '1131665y', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4194425', '111462LP', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4194453', '113248YE', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4194457', '11258Iuu', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4194459', '11578Yjo', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4194465', '11184SH5', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4194472', '112482zm', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4194486', '11355dBN', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4194488', '113561jr', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4194493', '11108vlu', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4194494', '1191MZS', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4194496', '11150ULc', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4194497', '11327dEF', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4194501', '11130jgQ', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4194506', '11198P6c', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4194508', '11284WMs', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4194509', '11250M9B', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4194510', '11104myv', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4194517', '11235U7K', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4194518', '11273JO3', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4194520', '112169Vb', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4194522', '11366Zo6', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4194532', '11367gs5', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4194536', '11369LCh', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4194538', '11251362', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4194544', '112247qU', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4194548', '11361710', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4194549', '11370kkd', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4194559', '11153jLF', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4194561', '11371YU6', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4194567', '11147A0X', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4194570', '11219gDM', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4194578', '11211dSl', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4194634', '11237f8F', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4194635', '11181vYY', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4194642', '11299vKp', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4194666', '11205Utz', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4194698', '11120jku', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4194700', '11117Fyv', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4194709', '11372nQH', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4194710', '111316cx', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4194711', '11374ZkA', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4194712', '11375qJ0', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4194715', '11378060', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4194716', '11232C5Z', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4194717', '11379AG3', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4194718', '11380qE7', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4194721', '113819dR', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4194722', '113821rX', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4194723', '113831jZ', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4194750', '11260e2r', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4194754', '11234bf5', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4194756', '11244RD3', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4194759', '11286wm9', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4194765', '11208kOG', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4194766', '1126705t', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4194771', '11285VME', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4194772', '11287Vyz', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4194779', '11276iXu', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4194788', '11123vJx', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4194792', '11256fmL', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4194799', '11225Joj', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4194804', '11351GaE', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4194816', '11266f35', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4194817', '11144eba', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4194818', '1196oOi', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4194823', '11270iy7', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4194824', '11209Grk', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4194835', '11280Gnc', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4194840', '11101LEQ', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4194843', '11359Mqb', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4194860', '11646Gim', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4194886', '11328vaZ', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4194898', '1184nFU', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4194914', '11325U4L', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4194920', '112686E8', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4194927', '113628XQ', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4194954', '11200NGF', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4194962', '11201FDQ', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4194977', '11162YZR', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4194981', '11154hFV', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4194994', '11226qeq', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195002', '11240qKZ', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195004', '1189NiN', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195026', '1194xT0', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195040', '113051aq', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195058', '112313K4', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195059', '1197IgW', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195084', '11402K3S', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195089', '11252WIV', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195103', '112278Cg', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195106', '111618fi', '1', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195111', '11885yq', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195131', '1193Rid', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195137', '11138VF9', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195139', '11116bo5', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195145', '111375Lv', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195152', '1187rAy', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195153', '11297mTb', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195157', '11192PNw', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195161', '11126bQs', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195162', '11348ILf', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195173', '11100SVK', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195174', '11127lnO', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195184', '11255PzR', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195185', '11342irt', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195188', '112334sX', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195191', '11264Ov5', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195206', '11384tmw', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195208', '11385qtf', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195209', '113860lp', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195210', '11387AZv', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195211', '11388Lgo', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195212', '11390JaZ', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195213', '11392PmY', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195218', '113931AX', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195235', '11249aW7', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195250', '112785CV', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195251', '11238Mtv', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195252', '11206ZAO', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195281', '11333H90', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195307', '11308thz', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195310', '1198gGH', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195313', '112710dC', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195314', '11292Gdf', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195315', '11293DNQ', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195322', '11241LEw', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195326', '11259odv', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195331', '11304c8U', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195337', '11579PhY', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195357', '11236SwK', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195358', '111594U5', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195360', '11158eXb', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195369', '11160eo1', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195383', '11155flS', '1', '1', '1');
INSERT INTO `roleinvite` VALUES ('4195388', '11106JMj', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195391', '11128D1x', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195392', '11365Ytt', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195401', '1128146i', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195408', '11133yPD', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195420', '112902Y9', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195421', '1192An4', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195424', '11114nDg', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195440', '11110lL4', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195446', '1195zYZ', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195454', '11229b1B', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195465', '11105QzC', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195470', '112913Il', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195483', '1140331S', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195500', '11295o2P', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195502', '11245glc', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195506', '11113bOP', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195510', '11296NvE', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195551', '11139oVV', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195552', '11338d4s', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195553', '1123027h', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195554', '11107adH', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195557', '11320bdL', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195558', '11247sWa', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195559', '11395d8H', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195561', '1133704E', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195575', '11218RSx', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195586', '11210JQR', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195594', '11164i7K', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195611', '11121zs3', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195620', '11335j1E', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195636', '11394WVn', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195646', '111343Ye', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195687', '11349oyC', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195695', '11115Swv', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195699', '113305zM', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195725', '11336tmx', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195729', '11223XfO', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195755', '11188vRh', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195764', '11212mjy', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195768', '11368oJh', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195769', '11228hnq', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195771', '11272zsa', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195776', '112794u0', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195778', '11353tRG', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195779', '11196Y15', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195780', '11197bMw', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195788', '11302cOU', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195792', '11148B7X', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195795', '11221yPO', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195807', '11391K1n', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195815', '111037Zh', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195820', '11340IwM', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195821', '11322qDn', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195829', '11112rO0', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195830', '11389aJc', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195832', '11323V6n', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195837', '11341gb7', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195844', '11195vNY', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195848', '11346pwx', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195849', '11125351', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195855', '1190q26', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195856', '11319v67', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195857', '1185pq7', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195860', '11318lEQ', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195864', '11194Vr9', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195865', '11191i8y', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195875', '11199wPu', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195880', '11222qhH', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195888', '11294b2Y', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195890', '11215RvZ', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195891', '11311Ijd', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195892', '11310sFW', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195895', '11301sR4', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195896', '112831fU', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195900', '11118B9W', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195906', '113096zy', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195911', '11124gAx', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195913', '11275vP3', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195914', '11132oEv', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195915', '11402DkD', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195918', '112777ZT', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195919', '11289vuU', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195920', '11254ZjB', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195921', '11111TgJ', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195925', '1182FeD', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195933', '11156DM5', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195934', '111578JL', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195935', '11334B2C', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195938', '1199voK', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195939', '11109Eks', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195940', '11119otd', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195941', '11129IAu', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195942', '11136xzI', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195943', '11149B7N', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195944', '111519YM', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195945', '11152RC8', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195947', '11179Mrc', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195950', '111808D6', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195951', '11182c6F', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195952', '111833M3', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195953', '1118576h', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195954', '11186Sg2', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195955', '11189g1w', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195956', '11190dME', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195957', '11239xa6', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195958', '11243SNO', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195959', '11246cOg', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195960', '11253kU2', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195961', '11257ue6', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195962', '11262N07', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195963', '11263n4E', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195964', '11269n81', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195965', '11274LSW', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195966', '11282TQb', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195967', '11300j3g', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195968', '113033HH', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195969', '11306PP8', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195970', '11307ejO', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195971', '11312o82', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195972', '113137tA', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195973', '11314Nk0', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195974', '11315W10', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195975', '113175Mi', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195976', '11329egn', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195977', '11331x3h', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195978', '11332n89', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195979', '11344SeT', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195980', '11345Esh', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195981', '113477bT', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195982', '11350qoR', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195983', '11352Xa1', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195984', '11354Wn8', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195985', '11357PHa', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195986', '11358zn0', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195987', '11363Iih', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195988', '113641zg', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195989', '113735Jc', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195990', '11376FRD', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195991', '11377EH2', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4195994', '11403aZ2', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4196014', '115318Ee', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4196135', '11616um6', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4196151', '11617Yea', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4196154', '11582uIz', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('4196196', '11656TgL', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('41947234430', '115770rd', '0', '0', '1');
INSERT INTO `roleinvite` VALUES ('41947236105', '11530iw9', '0', '0', '1');

-- ----------------------------
-- Table structure for `welfareaccount`
-- ----------------------------
DROP TABLE IF EXISTS `welfareaccount`;
CREATE TABLE `welfareaccount` (
  `account` varchar(255) NOT NULL COMMENT '账号名称',
  PRIMARY KEY  (`account`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='福利账号表';

-- ----------------------------
-- Records of welfareaccount
-- ----------------------------
