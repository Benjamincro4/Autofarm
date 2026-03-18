SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for `community_skillsave`
-- ----------------------------
DROP TABLE IF EXISTS `community_skillsave`;
CREATE TABLE `community_skillsave` (
  `charId` int(10) DEFAULT NULL,
  `skills` text,
  `pet` text
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

