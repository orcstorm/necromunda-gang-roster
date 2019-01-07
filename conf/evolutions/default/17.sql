# DC schema
 
# --- !Ups

CREATE TABLE combi_fighters (
  id integer NOT NULL AUTO_INCREMENT PRIMARY KEY,
  fighter_id integer NOT NULL,
  combi_id integer NOT NULL
)


# --- !Downs

DROP TABLE combi_fighters