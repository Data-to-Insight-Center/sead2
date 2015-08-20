DROP TABLE IF EXISTS status;
CREATE TABLE status (
   status_id                VARCHAR(127) NOT NULL,
   component                VARCHAR(256) NOT NULL,
   description                 VARCHAR(256) NOT NULL,
   PRIMARY KEY (status_id)
) ENGINE=INNODB;

DROP TABLE IF EXISTS collection_status;
CREATE TABLE collection_status (
   collection_id            VARCHAR(256) NOT NULL,
   current_status           VARCHAR(256) NOT NULL,
   status_updated_time      BIGINT NOT NULL,
   PRIMARY KEY (collection_id, current_status),
--   FOREIGN KEY (collection_id) REFERENCES collection(entity_id),
   FOREIGN KEY (current_status) REFERENCES status(status_id)
) ENGINE=INNODB;


