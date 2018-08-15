# DC schema
 
# --- !Ups

CREATE TABLE weapon_traits (
  id integer NOT NULL AUTO_INCREMENT PRIMARY KEY,
  weapon_trait varchar(128) NOT NULL
)


# --- !Downs

DROP TABLE weapon_traits