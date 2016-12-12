CREATE TABLE IF NOT EXISTS `config` (
	`id`	INTEGER NOT NULL UNIQUE,
	`name`	TEXT NOT NULL UNIQUE,
	`language`	TEXT NOT NULL,
	`file`	TEXT,
	`exportdb`	TEXT,
	`dbuser`	TEXT,
	`dbpassword`	TEXT,
	PRIMARY KEY(`id`)
);