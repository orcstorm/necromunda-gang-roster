# DC schema
 
# --- !Ups

create table combi_weapons (
  id integer NOT NULL AUTO_INCREMENT PRIMARY KEY,
  combi_id integer NOT NULL,
  weapon_id integer NOT NULL	
);

# --- !Downs

DROP TABLE combi_weapons;