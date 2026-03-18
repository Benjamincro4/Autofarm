/*
Navicat MySQL Data Transfer

Source Server         : localhost_3306
Source Server Version : 50160
Source Host           : localhost:3306
Source Database       : l2jks

Target Server Type    : MYSQL
Target Server Version : 50160
File Encoding         : 65001

Date: 2013-09-06 11:56:49
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for `custom_npcaidata`
-- ----------------------------
DROP TABLE IF EXISTS `custom_npcaidata`;
CREATE TABLE `custom_npcaidata` (
  `npcId` mediumint(7) unsigned NOT NULL,
  `minSkillChance` tinyint(3) unsigned NOT NULL DEFAULT '7',
  `maxSkillChance` tinyint(3) unsigned NOT NULL DEFAULT '15',
  `primarySkillId` smallint(5) unsigned DEFAULT '0',
  `agroRange` smallint(4) unsigned NOT NULL DEFAULT '0',
  `canMove` tinyint(1) unsigned NOT NULL DEFAULT '1',
  `targetable` tinyint(1) unsigned NOT NULL DEFAULT '1',
  `showName` tinyint(1) unsigned NOT NULL DEFAULT '1',
  `minRangeSkill` smallint(5) unsigned DEFAULT '0',
  `minRangeChance` tinyint(3) unsigned DEFAULT '0',
  `maxRangeSkill` smallint(5) unsigned DEFAULT '0',
  `maxRangeChance` tinyint(3) unsigned DEFAULT '0',
  `soulShot` smallint(4) unsigned DEFAULT '0',
  `spiritShot` smallint(4) unsigned DEFAULT '0',
  `spsChance` tinyint(3) unsigned DEFAULT '0',
  `ssChance` tinyint(3) unsigned DEFAULT '0',
  `aggro` smallint(4) unsigned NOT NULL DEFAULT '0',
  `isChaos` smallint(4) unsigned DEFAULT '0',
  `clan` varchar(40) DEFAULT NULL,
  `clanRange` smallint(4) unsigned DEFAULT '0',
  `enemyClan` varchar(40) DEFAULT NULL,
  `enemyRange` smallint(4) unsigned DEFAULT '0',
  `dodge` tinyint(3) unsigned DEFAULT '0',
  `aiType` varchar(8) NOT NULL DEFAULT 'fighter',
  PRIMARY KEY (`npcId`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of custom_npcaidata
-- ----------------------------
INSERT INTO `custom_npcaidata` VALUES ('50007', '7', '15', '0', '1000', '0', '1', '1', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', null, '300', null, '0', '0', 'fighter');
INSERT INTO `custom_npcaidata` VALUES ('70010', '7', '15', '0', '1000', '0', '1', '1', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', null, '300', null, '0', '0', 'fighter');
INSERT INTO `custom_npcaidata` VALUES ('1000003', '7', '15', '0', '1000', '0', '1', '1', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', null, '300', null, '0', '0', 'fighter');
INSERT INTO `custom_npcaidata` VALUES ('900100', '7', '15', '0', '1000', '0', '1', '1', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', null, '300', null, '0', '0', 'fighter');
INSERT INTO `custom_npcaidata` VALUES ('900101', '7', '15', '0', '1000', '0', '1', '1', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', null, '300', null, '0', '0', 'fighter');
INSERT INTO `custom_npcaidata` VALUES ('900102', '7', '15', '0', '1000', '0', '1', '1', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', null, '300', null, '0', '0', 'fighter');
INSERT INTO `custom_npcaidata` VALUES ('900103', '7', '15', '0', '1000', '0', '1', '1', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', null, '300', null, '0', '0', 'fighter');
INSERT INTO `custom_npcaidata` VALUES ('900104', '7', '15', '0', '1000', '0', '1', '1', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', null, '300', null, '0', '0', 'fighter');
INSERT INTO `custom_npcaidata` VALUES ('70011', '7', '15', '0', '0', '1', '1', '1', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', null, '0', null, '0', '0', 'fighter');
INSERT INTO `custom_npcaidata` VALUES ('555', '7', '15', '0', '0', '1', '1', '1', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', null, '0', null, '0', '0', 'fighter');
INSERT INTO `custom_npcaidata` VALUES ('24610', '7', '15', '0', '0', '1', '1', '1', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', null, '0', null, '0', '0', 'fighter');
INSERT INTO `custom_npcaidata` VALUES ('36601', '7', '15', '0', '0', '1', '1', '1', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', null, '0', null, '0', '0', 'fighter');
INSERT INTO `custom_npcaidata` VALUES ('36602', '7', '15', '0', '0', '1', '1', '1', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', null, '0', null, '0', '0', 'fighter');
INSERT INTO `custom_npcaidata` VALUES ('36603', '7', '15', '0', '0', '1', '1', '1', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', null, '0', null, '0', '0', 'fighter');
INSERT INTO `custom_npcaidata` VALUES ('36604', '7', '15', '0', '0', '1', '1', '1', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', null, '0', null, '0', '0', 'fighter');
INSERT INTO `custom_npcaidata` VALUES ('65535', '7', '15', '0', '0', '1', '1', '1', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', null, '0', null, '0', '0', 'fighter');
INSERT INTO `custom_npcaidata` VALUES ('9000', '7', '15', '0', '0', '1', '1', '1', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', null, '0', null, '0', '0', 'fighter');
INSERT INTO `custom_npcaidata` VALUES ('9001', '7', '15', '0', '0', '1', '1', '1', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', null, '0', null, '0', '0', 'fighter');
INSERT INTO `custom_npcaidata` VALUES ('9002', '7', '15', '0', '0', '1', '1', '1', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', null, '0', null, '0', '0', 'fighter');
INSERT INTO `custom_npcaidata` VALUES ('9003', '7', '15', '0', '0', '1', '1', '1', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', null, '0', null, '0', '0', 'fighter');
INSERT INTO `custom_npcaidata` VALUES ('222199', '7', '15', '0', '0', '1', '1', '1', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', null, '0', null, '0', '0', 'fighter');
INSERT INTO `custom_npcaidata` VALUES ('222217', '7', '15', '0', '0', '1', '1', '1', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', null, '0', null, '0', '0', 'fighter');
INSERT INTO `custom_npcaidata` VALUES ('222223', '7', '15', '0', '0', '1', '1', '1', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', null, '0', null, '0', '0', 'fighter');
INSERT INTO `custom_npcaidata` VALUES ('51', '7', '15', '0', '0', '1', '1', '1', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', null, '0', null, '0', '0', 'fighter');
INSERT INTO `custom_npcaidata` VALUES ('36606', '7', '15', '0', '0', '1', '1', '1', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', null, '0', null, '0', '0', 'fighter');
INSERT INTO `custom_npcaidata` VALUES ('36607', '7', '15', '0', '0', '1', '1', '1', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', null, '0', null, '0', '0', 'fighter');
INSERT INTO `custom_npcaidata` VALUES ('36608', '7', '15', '0', '0', '1', '1', '1', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', null, '0', null, '0', '0', 'fighter');
INSERT INTO `custom_npcaidata` VALUES ('36610', '7', '15', '0', '0', '1', '1', '1', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', null, '0', null, '0', '0', 'fighter');
INSERT INTO `custom_npcaidata` VALUES ('7104', '7', '15', '0', '0', '1', '1', '1', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', null, '0', null, '0', '0', 'fighter');
INSERT INTO `custom_npcaidata` VALUES ('32639', '7', '15', '0', '0', '1', '1', '1', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', null, '0', null, '0', '0', 'fighter');
INSERT INTO `custom_npcaidata` VALUES ('13182', '7', '15', '0', '0', '1', '1', '1', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', null, '0', null, '0', '0', 'fighter');
INSERT INTO `custom_npcaidata` VALUES ('9023', '7', '15', '0', '0', '1', '1', '1', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', null, '0', null, '0', '0', 'fighter');
INSERT INTO `custom_npcaidata` VALUES ('36609', '7', '15', '0', '0', '1', '1', '1', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', null, '0', null, '0', '0', 'fighter');
INSERT INTO `custom_npcaidata` VALUES ('36605', '7', '15', '0', '0', '1', '1', '1', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', null, '0', null, '0', '0', 'fighter');
