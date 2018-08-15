# DC schema
 
# --- !Ups

CREATE TABLE fighter_profiles (
  id integer NOT NULL AUTO_INCREMENT PRIMARY KEY,
  house_id integer NOT NULL,
  fighter_class varchar(32),
  move  integer NOT NULL,
  weapon_skill integer NOT NULL,
  ballistic_skill integer NOT NULL,
  strength integer NOT NULL,
  toughness integer NOT NULL,
  wounds integer NOT NULL,
  initiative integer NOT NULL,
  attacks integer NOT NULL,
  leadership integer NOT NULL,
  cool integer NOT NULL,
  willpower integer NOT NULL,
  intelligence integer NOT NULL,
  cost integer NOT NULL
)


# -- !Downs

DROP TABLE fighter_profiles
