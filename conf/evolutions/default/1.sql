# DC schema
 
# --- !Ups

CREATE TABLE weapons (
  id integer NOT NULL AUTO_INCREMENT PRIMARY KEY,
  weapon_type varchar(64) NOT NULL,
  name varchar(128) NOT NULL,
  variant varchar(128),
  rng_short varchar(8),
  rng_long varchar(8),
  accShort varchar(8),
  accLong varchar(8),
  strength varchar(8),
  armor_pen varchar(8),
  damage varchar(8),
  ammo varchar(8)
);


# --- !Downs

DROP TABLE weapons;