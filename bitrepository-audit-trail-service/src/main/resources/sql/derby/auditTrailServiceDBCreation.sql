---
-- #%L
-- Bitrepository Audit Trail Service
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

connect 'jdbc:derby:auditservicedb;create=true';

--**************************************************************************--
-- Name:        tableversions
-- Description: This table contains an overview of the different tables 
--              within this database along with their respective versions.
-- Purpose:     To keep track of the versions of the tables within the 
--              database. Used for differentiating between different version
--              of the tables, especially when upgrading.
-- Expected entry count: only the tables in this script.
--**************************************************************************--
create table tableversions (
    tablename varchar(100) not null, -- Name of table
    version int not null             -- version of table
);

insert into tableversions ( tablename, version ) values ( 'audittrail', 2);
insert into tableversions ( tablename, version ) values ( 'file', 2);
insert into tableversions ( tablename, version ) values ( 'contributor', 2);
insert into tableversions ( tablename, version ) values ( 'actor', 2);
insert into tableversions ( tablename, version ) values ( 'collection', 1);
insert into tableversions ( tablename, version ) values ( 'auditservicedb', 2);

--*************************************************************************--
-- Name:     file
-- Descr.:   Container for the files ids and their keys.
-- Purpose:  Keeps track of the different file ids. 
-- Expected entry count: A lot. Though not as many as 'audittrail'.
--*************************************************************************--
create table file (
    file_key bigint not null generated always as identity primary key,
                                    -- The key for the entry in the file table.
    fileid varchar(255),            -- The actual file id.
    collection_key bigint not null, -- The key for the collection for the file.
    UNIQUE ( fileid )
);

--create index fileindex on file ( fileid );
create index filecollectionindex on file ( fileid, collection_key );

--*************************************************************************--
-- Name:     collection
-- Descr.:   Container for the collection ids and their keys.
-- Purpose:  Keeps track of the different collection ids. 
-- Expected entry count: very few. 
--*************************************************************************--
create table collection (
    collection_key bigint not null generated always as identity primary key,
                                    -- The key for the entry in the collection table.
    collectionid varchar(255),      -- The actual id of the collection.
    UNIQUE ( collectionid )
);

--create index collectionindex on collection ( collectionid );

--*************************************************************************--
-- Name:     contributor
-- Descr.:   Container for the contributors ids and their guids.
-- Purpose:  Keeps track of the different contributor ids. 
-- Expected entry count: Few. Only the pillars and services for the 
--                       collection are contributors of audit trails.
--*************************************************************************--
create table contributor (
    contributor_key bigint not null generated always as identity primary key,
                                    -- The key for the contributor id.
    contributor_id varchar(255),    -- The actual id of the contributor.
    UNIQUE ( contributor_id )
);

--create index contributorindex on contributor ( contributor_id );

--*************************************************************************--
-- Name:     actor
-- Descr.:   Contains the name of an actor.
-- Purpose:  Keeps track of the different actors.
-- Expected entry count: Some, though not many.
--*************************************************************************--
create table actor (
    actor_key bigint not null generated always as identity primary key,
                                    -- The key for the actor.
    actor_name varchar(255),         -- The name of the actor.
    UNIQUE ( actor_name )
);

--create index actorindex on actor ( actor_name );

--*************************************************************************--
-- Name:     preservation
-- Descr.:   Container for the preservation of audit trails based on 
--           contributors per collection.
-- Purpose:  Keeps track of the sequence number reached by the preservation
--           for each contributor per collection. 
-- Expected entry count: Few. Only the pillars and services for each 
--                       collection are contributors of audit trails.
--*************************************************************************--
create table preservation (
    preservation_key bigint not null generated always as identity primary key,
                                    -- The key for the preservation id.
    contributor_key bigint,         -- The key of the contributor.
    collection_key bigint,          -- The key for the collection.
    preserved_seq_number bigint,    -- The sequence number reached for the preservation
                                    -- of the audit trails for the contributor.
    FOREIGN KEY (contributor_key) REFERENCES contributor(contributor_key),
                                    -- Foreign key constraint on pillar_key, enforcing the presence of the referred id
    FOREIGN KEY (collection_key) REFERENCES collection(collection_key),
                                    -- Foreign key constraint on pillar_key, enforcing the presence of the referred id
    UNIQUE (collection_key, contributor_key)        
                                    -- Enforce that each contributor only can exist once per collection.
);

--*************************************************************************--
-- Name:     audittrail
-- Descr.:   Container for the audits with their sequence number, the guid
--           for the file, the action which cause the audit, the id for the
--           actor, and the date for the audit.
-- Purpose:  Keeps track of the different audits.
-- Expected entry count: Very, very many.
--*************************************************************************--
create table audittrail (
    audit_key bigint not null generated always as identity primary key,
                                    -- The key for this table.
    sequence_number bigint not null,-- The sequence number for the given audit trail.
    contributor_key bigint not null,
                                    -- The identifier for the contributor of this audittrail.
                                    -- Used for looking up in the contributor table.
    file_key bigint not null,       -- The identifier for the file. Used to lookup in the file table.
    actor_key bigint not null,      -- The identifier for the actor which performed the action for the audit. 
                                    -- Used for looking up in the actor table.
    operation varchar(100),         -- The name of the action behind the audit.
    operation_date timestamp,       -- The date when the action was performed.
    audit varchar(255),             -- The audit trail delivered from the actor. 
    information varchar(255),       -- The information about the audit.
    
    FOREIGN KEY (contributor_key) REFERENCES contributor(contributor_key),
                                 -- Foreign key constraint on pillar_key, enforcing the presence of the referred id
    FOREIGN KEY (file_key) REFERENCES file(file_key),
                                 -- Foreign key constraint on file_key, enforcing the presence of the referred id                                 
    FOREIGN KEY (actor_key) REFERENCES actor(actor_key)
                                 -- Foreign key constraint on pillar_key, enforcing the presence of the referred id

);

create index dateindex on audittrail ( operation_date );
create index auditindex on audittrail ( contributor_key, file_key, actor_key );