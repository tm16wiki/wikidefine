--sqlite
CREATE TABLE IF NOT EXISTS `definition` (
	`id`	INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE,
	`title`	TEXT NOT NULL UNIQUE,
	`text`	TEXT NOT NULL
);

--postgresql
CREATE TABLE IF NOT EXISTS definition (
	id	SERIAL  PRIMARY KEY,
	title TEXT NOT NULL UNIQUE,
	text	TEXT NOT NULL,
	PRIMARY KEY(id)
);

--TODO: mysql