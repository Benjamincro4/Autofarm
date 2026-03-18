CREATE TABLE IF NOT EXISTS `character_services` (
  `charId` int(10) unsigned NOT NULL,
  `serviceName` varchar(50) NOT NULL,
  `expiration` bigint(13) unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`charId`,`serviceName`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
