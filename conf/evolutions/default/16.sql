# DC schema
 
# --- !Ups

CREATE TABLE combis_cost (
  id integer NOT NULL AUTO_INCREMENT PRIMARY KEY,
  house_id integer NOT NULL,
  combi_id integer NOT NULL,
  credits integer NOT NULL
)


# --- !Downs

DROP TABLE combis_cost