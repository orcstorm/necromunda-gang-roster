# DC schema
 
# --- !Ups

CREATE TABLE fighter_wargear (
  id integer NOT NULL AUTO_INCREMENT PRIMARY KEY,
  fighter_id integer NOT NULL,
  wargear_id integer NOT NULL 
);

# --- !Downs

DROP TABLE fighter_wargear;