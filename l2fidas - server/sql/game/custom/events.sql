DROP TABLE IF EXISTS `events`;
CREATE TABLE `events` (
  `id` int(11) NOT NULL,
  `name` varchar(40) NOT NULL,
  `month` int(11) NOT NULL,
  `start_day` int(11) NOT NULL,
  `end_day` int(11) NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;


INSERT INTO `events` VALUES 
('1', 'MasterOfEnchanting', '1', '1', '28'),
('2', 'TheValentineEvent', '2', '1', '26'),
('3', 'HollyCow', '3', '1', '28'),
('4', 'AprilFools', '4', '1', '7'),
('5', 'NinjaAdventures', '9', '8', '28'),
('6', 'SuperStar', '9', '1', '26'),
('7', 'SquashEvent', '10', '1', '28'),
('8', 'L2Day', '5', '1', '30'),
('9', 'NewEra', '8', '1', '30'),
('10', 'SchoolDays', '6', '1', '30'),
('11', 'TrickorTransmutation', '7', '1', '30'),
('12', 'HallowedYou', '11', '1', '30'),
('13', 'ChristmasIsHere', '12', '1', '31');