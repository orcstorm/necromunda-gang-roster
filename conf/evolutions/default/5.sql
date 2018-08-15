# DC schema
 
# --- !Ups

CREATE TABLE fighter_weapon(
  id integer NOT NULL AUTO_INCREMENT PRIMARY KEY,
  fighter_id integer NOT NULL,
  weapon_id integer NOT NULL
);

# --- !Downs

DROP TABLE fighter_weapon