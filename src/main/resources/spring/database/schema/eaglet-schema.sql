CREATE TABLE IF NOT EXISTS Users (
userid int PRIMARY KEY,
name VARCHAR(50)
);
CREATE TABLE IF NOT EXISTIS Documents (
userId int NOT NULL,
documentUri VARCHAR(200) NOT NULL,
fileName VARCHAR(100) NOT NULL,
PRIMARY KEY (userId, documentUri)
);