SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for `hitman_list`
-- ----------------------------
DROP TABLE IF EXISTS `hitman_list`;
CREATE TABLE `hitman_list` (
  `targetId` int(16) NOT NULL default '0',
  `clientId` int(16) NOT NULL default '0',
  `target_name` varchar(24) NOT NULL default '',
  `bounty` int(30) NOT NULL default '0',
  `pending_delete` int(16) NOT NULL default '0',
  PRIMARY KEY  (`targetId`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;