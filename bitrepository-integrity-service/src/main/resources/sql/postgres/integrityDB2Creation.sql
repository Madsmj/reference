---
-- #%L
-- Bitrepository Integrity Client
-- %%
-- Copyright (C) 2010 - 2012 The State and University Library, The Royal Library and The State Archives, Denmark
-- %%
-- This program is free software: you can redistribute it and/or modify
-- it under the terms of the GNU Lesser General Public License as 
-- published by the Free Software Foundation, either version 2.1 of the 
-- License, or (at your option) any later version.
-- 
-- This program is distributed in the hope that it will be useful,
-- but WITHOUT ANY WARRANTY; without even the implied warranty of
-- MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
-- GNU General Lesser Public License for more details.
-- 
-- You should have received a copy of the GNU General Lesser Public 
-- License along with this program.  If not, see
-- <http://www.gnu.org/licenses/lgpl-2.1.html>.
-- #L%
---
-- HEADER

--**************************************************************************--
-- Name:        tableversions
-- Description: This table contains an overview of the different tables 
--              within this database along with their respective versions.
-- Purpose:     To keep track of the versions of the tables within the 
--              database. Used for differentiating between different version
--              of the tables, especially when upgrading.
-- Expected entry count: only those in this script.
--**************************************************************************--
CREATE TABLE tableversions (
    tablename VARCHAR(100) NOT NULL, -- Name of table
    version SMALLINT NOT NULL        -- version of table
);

INSERT INTO tableversions (tablename, version) VALUES ('fileinfo', 3);
INSERT INTO tableversions (tablename, version) VALUES ('files', 2);
INSERT INTO tableversions (tablename, version) VALUES ('pillar', 2);
INSERT INTO tableversions (tablename, version) VALUES ('collections' ,1);
INSERT INTO tableversions (tablename, version) VALUES ('integritydb', 4);
INSERT INTO tableversions (tablename, version) VALUES ('stats', 1);
INSERT INTO tableversions (tablename, version) VALUES ('collectionstats', 1);
INSERT INTO tableversions (tablename, version) VALUES ('pillarstats', 1);

--*************************************************************************--
-- Name:     collections
-- Descr.:   Contains the information about the collections.
-- Purpose:  Keeps track of the names of the files within the system.
-- Expected entry count: Few
--*************************************************************************--
CREATE TABLE collections (
    collectionID VARCHAR(255) PRIMARY KEY  -- The id for the collection.
);


--*************************************************************************--
-- Name:     pillar
-- Descr.:   Contains the information about the pillars.
-- Purpose:  Keeps track of the information about the pillars.
-- Expected entry count: Few
--*************************************************************************--
CREATE TABLE pillar (
    pillarID VARCHAR(100) PRIMARY KEY   -- The id of the pillar.
);


CREATE TABLE fileinfo (
    fileID VARCHAR(255) NOT NULL,           -- The file ID 
    collectionID VARCHAR(255) NOT NULL,     -- The collection ID
    pillarID VARCHAR(255) NOT NULL,         -- The pillar ID
    filesize BIGINT,                        -- Size of the file
    checksum VARCHAR(100),                  -- The checksum of the file
    file_timestamp TIMESTAMP,               -- The last modified time on the pillar
    checksum_timestamp TIMESTAMP,           -- The calculation timestamp of the checksum
    last_seen_getfileids TIMESTAMP,         -- The last time the file was seen on a list of fileIDs for the pillar
    last_seen_getchecksums TIMESTAMP,       -- The last time the files was seen on a list of checksums for the pillar

    PRIMARY KEY (collectionID, pillarID, fileID),
    FOREIGN KEY (collectionID) REFERENCES collections(collectionID),
    FOREIGN KEY (pillarID) REFERENCES pillar(pillarID)
);

CREATE INDEX checksumdateindex ON fileinfo(checksum_timestamp);
CREATE INDEX lastseenindex ON fileinfo(last_seen_getfileids);

CREATE TABLE collection_progress (
    collectionID VARCHAR(255) NOT NULL,
    pillarID VARCHAR(100) NOT NULL,
    latest_file_timestamp TIMESTAMP DEFAULT NULL,
    latest_checksum_timestamp TIMESTAMP DEFAULT NULL
);

--*************************************************************************--
-- Name:     statistics 
-- Descr.:   Contains the information collected statistics.
-- Purpose:  Keeps track of the collected statistics.
-- Expected entry count: Many (over time)
--*************************************************************************--
CREATE TABLE stats (
    stat_key SERIAL PRIMARY KEY, -- The key for a set of statistics.
    stat_time TIMESTAMP NOT NULL, 
                                 -- The time the statistics entry were made.
    last_update TIMESTAMP NOT NULL,
                                 -- The last time the statistics were updated
    collectionID VARCHAR(255) NOT NULL, -- The key of the collection that the statistics belongs to 
    FOREIGN KEY (collectionID) REFERENCES collections(collectionID)
                                 -- Foreign key constraint on collection_key, enforcing the presence of the referred key
);

CREATE INDEX lastupdatetimeindex ON stats (last_update);

--*************************************************************************--
-- Name:     collectionstats
-- Descr.:   Contains the information about collection statistics.
-- Purpose:  Keeps track of the statistics for a collection.
-- Expected entry count: many (over time)
--*************************************************************************--
CREATE TABLE collectionstats (
    collectionstat_key SERIAL PRIMARY KEY,
                                 -- The key for the collectionstat.
    stat_key INT NOT NULL,       -- The key for the statistics entity.
    file_count INT,              -- The number of files that the collection contained when the stats were made
    file_size BIGINT,            -- The total size of the files in the collection when the stats were made
    checksum_errors_count INT,   -- The number of checksum errors in the collection when the stats were made
    UNIQUE (stat_key),           -- Enforce that there can only be one collectionstat for a statistics
    FOREIGN KEY (stat_key) REFERENCES stats(stat_key)
                                 -- Foreign key constraint on stat_key, enforcing the presence of the referred key
);

--*************************************************************************--
-- Name:     pillarstats
-- Descr.:   Contains the information about pillar statistics.
-- Purpose:  Keeps track of the statistics for a pillar.
-- Expected entry count: many (over time)
--*************************************************************************--
CREATE TABLE pillarstats (
    pillarstat_key SERIAL PRIMARY KEY,
                                 -- The key for the pillarstat.
    stat_key INT NOT NULL,       -- The key for the statistics entity.
    pillarID VARCHAR(100) NOT NULL,     -- The key of the pillar that the statistics belongs to 
    file_count INT,              -- The number of files on the pillar when the stats were made
    file_size BIGINT,            -- The total size of the files on the pillar when the stats were made
    missing_files_count INT,     -- The number of the missing files on the pillar when the stats were made
    checksum_errors_count INT,   -- The number of checksum errors on the pillar when the stats were made
    UNIQUE (stat_key, pillarID), 
                                 -- Enforce that there can only be one collectionstat for a statistics
    FOREIGN KEY (stat_key) REFERENCES stats(stat_key),
                                 -- Foreign key constraint on stat_key, enforcing the presence of the referred key
    FOREIGN KEY (pillarID) REFERENCES pillar(pillarID)
                                 -- Foreign key constraint on collection_key, enforcing the presence of the referred key
);
