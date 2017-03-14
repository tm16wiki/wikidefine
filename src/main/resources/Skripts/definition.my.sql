CREATE TABLE IF NOT EXISTS `textmining`.`definition` (
  `id` INT NOT NULL,
  `title` TEXT NOT NULL,
  `text` TEXT NOT NULL,
  `wikititle` TEXT NULL,
  PRIMARY KEY (`id`));