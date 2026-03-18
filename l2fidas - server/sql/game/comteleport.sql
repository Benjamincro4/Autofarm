SET FOREIGN_KEY_CHECKS=0;

DROP TABLE IF EXISTS `comteleport`;

CREATE TABLE `comteleport` (

  `TpId` int(11) NOT NULL AUTO_INCREMENT,
  
  `charId` int(11) DEFAULT NULL,
  
  `Xpos` text,

  `Ypos` text,

  `Zpos` text,

  `name` text,

  PRIMARY KEY (`TpId`)

) ENGINE=MyISAM AUTO_INCREMENT=321 DEFAULT CHARSET=cp1251;
