# DC schema
 
# --- !Ups

CREATE TABLE skills (
  id integer NOT NULL AUTO_INCREMENT PRIMARY KEY,
  name varchar(128) NOT NULL, 
  skill_type varchar(128) NOT NULL,
  description varchar(4096) NOT NULL
);


# --- !Downs

DROP TABLE skills;