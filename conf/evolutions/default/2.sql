# DC schema
 
# --- !Ups

CREATE TABLE fighters (
  id integer NOT NULL AUTO_INCREMENT PRIMARY KEY,
  gang_id integer NOT NULL,
  name varchar(128) NOT NULL, 
  fighter_type integer NOT NULL
)


# --- !Downs

DROP TABLE fighters