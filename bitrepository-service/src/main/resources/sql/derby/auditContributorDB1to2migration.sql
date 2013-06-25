---
-- #%L
-- Bitrepository Integrity Service
-- %%
-- Copyright (C) 2010 - 2013 The State and University Library, The Royal Library and The State Archives, Denmark
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
-- Integrity DB migration from version 1 to 2

connect 'jdbc:derby:auditcontributerdb';

-- Update table versions.
UPDATE tableversions SET version=2 WHERE tablename='audit';

-- Add constraints to audittrail table.
ALTER TABLE audittrail (
    ADD FOREIGN KEY ( file_guid ) REFERENCES file ( file_guid ),
    ADD FOREIGN KEY ( actor_guid ) REFERENCES actor ( actor_guid )
);
