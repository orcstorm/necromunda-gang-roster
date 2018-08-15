# DC schema
 
# --- !Ups

CREATE TABLE weapons_cost (
  id integer NOT NULL AUTO_INCREMENT PRIMARY KEY,
  house_id integer NOT NULL,
  weapon_id integer NOT NULL,
  credits integer NOT NULL
)


# --- !Downs

DROP TABLE weapons_cost