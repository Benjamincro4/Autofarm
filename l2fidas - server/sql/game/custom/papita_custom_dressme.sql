ALTER TABLE `accounts` ADD COLUMN `dressme_armor_sets` TEXT NULL DEFAULT NULL;
ALTER TABLE `characters` ADD COLUMN `dressme_armor_set` INT NOT NULL DEFAULT 0;