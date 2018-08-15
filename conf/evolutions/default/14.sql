# DC schema
 
# --- !Ups

CREATE TABLE combis (
  id integer NOT NULL AUTO_INCREMENT PRIMARY KEY,
  name varchar(64) NOT NULL
);


# --- !Downs

DROP TABLE combis;