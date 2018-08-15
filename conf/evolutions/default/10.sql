# DC schema
 
# --- !Ups

CREATE TABLE fighter_skills (
  id integer NOT NULL AUTO_INCREMENT PRIMARY KEY,
  fighter_id integer NOT NULL,
  skill_id integer NOT NULL 
);

# --- !Downs

DROP TABLE fighter_skills;