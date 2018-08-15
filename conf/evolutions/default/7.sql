# DC schema
 
# --- !Ups

CREATE TABLE weapons_traits (
  id integer NOT NULL AUTO_INCREMENT PRIMARY KEY,
  weapon_id integer NOT NULL,
  trait_id integer NOT NULL
)


# --- !Downs

DROP TABLE weapons_traits