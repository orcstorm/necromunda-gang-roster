# DC schema
 
# --- !Ups

CREATE TABLE gangs (
  id integer NOT NULL AUTO_INCREMENT PRIMARY KEY,
  house integer NOT NULL, 
  name varchar(128) NOT NULL
);

CREATE TABLE houses (
  id integer NOT NULL AUTO_INCREMENT PRIMARY KEY,
  name varchar(32) NOT NULL
);

# --- !Downs

DROP TABLE gangs;
DROP TABLE houses;