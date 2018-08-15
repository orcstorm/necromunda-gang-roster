# DC schema
 
# --- !Ups

CREATE TABLE wargear (
  id integer NOT NULL AUTO_INCREMENT PRIMARY KEY,
  name varchar(4096) NOT NULL,
  description varchar(4096) NOT NULL 
);

# --- !Downs

DROP TABLE wargear;