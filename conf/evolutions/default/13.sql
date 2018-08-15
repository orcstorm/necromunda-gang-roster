# DC schema
 
# --- !Ups

CREATE TABLE wargear_cost (
  id integer NOT NULL AUTO_INCREMENT PRIMARY KEY,
  house_id integer NOT NULL,
  wargear_id integer NOT NULL,
  credits integer NOT NULL
)


# --- !Downs

DROP TABLE wargear_cost