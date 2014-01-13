package org.bitrepository.integrityservice.cache.database;

import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.COLLECTION_KEY;
import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.COLLECTION_STATS_TABLE;
import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.CS_CHECKSUM_ERRORS;
import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.CS_FILECOUNT;
import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.CS_FILESIZE;
import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.CS_STAT_KEY;
import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.FILES_ID;
import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.FILES_KEY;
import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.FILES_TABLE;
import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.FILE_INFO_TABLE;
import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.FI_FILE_KEY;
import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.FI_FILE_STATE;
import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.FI_PILLAR_KEY;
import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.PILLAR_ID;
import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.PILLAR_KEY;
import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.PILLAR_TABLE;
import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.STATS_COLLECTION_KEY;
import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.STATS_KEY;
import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.STATS_LAST_UPDATE;
import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.STATS_TABLE;
import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.STATS_TIME;

import org.bitrepository.service.database.DatabaseManager;
import org.bitrepository.settings.repositorysettings.Collections;

/**
 * DAO class implementing the specifics for a Postgres based backend for the integrityDB 
 */
public class PostgresIntegrityDAO extends IntegrityDAO {

    public PostgresIntegrityDAO(DatabaseManager databaseManager, Collections collections) {
        super(databaseManager, collections);
    }

    @Override
    protected String getLatestCollectionStatsSQL() {
        return "SELECT c." + CS_FILECOUNT +  ", c." + CS_FILESIZE + ", c." + CS_CHECKSUM_ERRORS
                + ", s." + STATS_TIME + ", s." + STATS_LAST_UPDATE 
                + " FROM " + COLLECTION_STATS_TABLE + " c "
                + " JOIN " + STATS_TABLE + " s" 
                + " ON  c." + CS_STAT_KEY + " = s." + STATS_KEY
                + " WHERE s." + STATS_COLLECTION_KEY + " = ?"
                + " ORDER BY s." + STATS_TIME + " DESC "
                + " LIMIT ?";
    }

    @Override
    protected String getLatestStatisticsKeySQL() {
        return "SELECT " + STATS_KEY + " FROM " + STATS_TABLE
                + " WHERE " + STATS_COLLECTION_KEY + " = ?"
                + " ORDER BY " + STATS_KEY + " DESC"
                + " LIMIT 1";
    }

    @Override
    protected String getMissingFilesOnPillarSql() {
        String selectSql = "SELECT " + FILES_TABLE + "." + FILES_ID + " FROM " + FILES_TABLE 
                + " JOIN " + FILE_INFO_TABLE 
                + " ON " + FILES_TABLE + "." + FILES_KEY + "=" + FILE_INFO_TABLE + "." + FI_FILE_KEY 
                + " WHERE " + FILE_INFO_TABLE + "." + FI_FILE_STATE + " = ?"
                + " AND "+ FILES_TABLE + "." + COLLECTION_KEY + "= ?" 
                + " AND " + FILE_INFO_TABLE + "." + FI_PILLAR_KEY + " = ("
                    + " SELECT " + PILLAR_KEY + " FROM " + PILLAR_TABLE 
                    + " WHERE " + PILLAR_ID + " = ? )"
                + " ORDER BY " + FILES_TABLE + "." + FILES_KEY
                + " OFFSET ?"
                + " LIMIT ?";
        
        return selectSql;
    }
    
    @Override
    protected String getFilesOnPillarSql() {
        String selectSql = "SELECT " + FILES_TABLE + "." + FILES_ID + " FROM " + FILES_TABLE 
                + " JOIN " + FILE_INFO_TABLE 
                + " ON " + FILES_TABLE + "." + FILES_KEY + "=" + FILE_INFO_TABLE + "." + FI_FILE_KEY 
                + " WHERE " + FILE_INFO_TABLE + "." + FI_FILE_STATE + " = ?"
                + " AND " + FILES_TABLE + "." + COLLECTION_KEY + "= ?"
                + " AND " + FILE_INFO_TABLE + "." + FI_PILLAR_KEY + " = ("
                    + " SELECT " + PILLAR_KEY + " FROM " + PILLAR_TABLE 
                    + " WHERE " + PILLAR_ID + " = ?)" 
                + " ORDER BY " + FILES_TABLE + "." + FILES_KEY
                + " OFFSET ?"
                + " LIMIT ?";
        
        return selectSql;
    }

    
}
