-- ----------------------------
-- Table structure for `votes`
-- ----------------------------
DROP TABLE IF EXISTS `votes`;
CREATE TABLE `votes` (
`id` int(6) NOT NULL,
`vote` int(6) NOT NULL,
PRIMARY KEY (`id`)
) DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of votes
-- ----------------------------
INSERT INTO votes VALUES ('1', '0');
