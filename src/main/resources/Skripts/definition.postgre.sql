--postgresql
CREATE TABLE IF NOT EXISTS definition (
	id	SERIAL  PRIMARY KEY,
	title TEXT NOT NULL UNIQUE,
	text	TEXT NOT NULL,
	wikititle text
);