package org.bitrepository.audittrails.store;

import static org.bitrepository.audittrails.store.AuditDatabaseExtractor.POSITION_ACTOR_NAME;
import static org.bitrepository.audittrails.store.AuditDatabaseExtractor.POSITION_AUDIT_TRAIL;
import static org.bitrepository.audittrails.store.AuditDatabaseExtractor.POSITION_CONTRIBUTOR_ID;
import static org.bitrepository.audittrails.store.AuditDatabaseExtractor.POSITION_FILE_ID;
import static org.bitrepository.audittrails.store.AuditDatabaseExtractor.POSITION_INFORMATION;
import static org.bitrepository.audittrails.store.AuditDatabaseExtractor.POSITION_OPERATION;
import static org.bitrepository.audittrails.store.AuditDatabaseExtractor.POSITION_OPERATION_DATE;
import static org.bitrepository.audittrails.store.AuditDatabaseExtractor.POSITION_SEQUENCE_NUMBER;
import static org.bitrepository.audittrails.store.AuditDatabaseExtractor.POSITION_OPERATION_ID;
import static org.bitrepository.audittrails.store.AuditDatabaseExtractor.POSITION_FINGERPRINT;


import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bitrepository.bitrepositoryelements.AuditTrailEvent;
import org.bitrepository.bitrepositoryelements.FileAction;
import org.bitrepository.common.utils.CalendarUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to iterate over the set of AuditTrailEvents produced by a resultset.  
 */
public class AuditEventIterator {

    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());
    private ResultSet auditResultSet = null;
    private Connection conn = null;
    private final PreparedStatement ps;
    
    /**
     * Constructor
     * @param auditResultSet The ResultSet of audit trails from the database
     * @param dbConnector The database connection, for looking up foreign keys in the auditResultSet 
     */
    public AuditEventIterator(PreparedStatement ps) {
        this.ps = ps;
    }
    
    /**
     * Method to explicitly close the ResultSet in the AuditEventIterator 
     * @throws SQLException in case of a sql error
     */
    public void close() throws SQLException {
        if(auditResultSet != null) {
            auditResultSet.close();
        }
        
        if(ps != null) {
            ps.close();
        }
        
        if(conn != null) {
            conn.setAutoCommit(true);
            conn.close();
        }
    }
    
    /**
     * Method to return the next AuditTrailEvent in the ResultSet
     * When no more AuditTrailEvents are available, null is returned and the internal ResultSet closed. 
     * @return The next AuditTrailEvent available in the ResultSet, or null if no more events are available. 
     * @throws SQLException In case of a sql error. 
     */
    public AuditTrailEvent getNextAuditTrailEvent() {
        try {
            AuditTrailEvent event = null;
            if(auditResultSet == null) {
                conn = ps.getConnection();
                conn.setAutoCommit(false);
                ps.setFetchSize(100);
                long tStart = System.currentTimeMillis();
                log.debug("Executing query to get AuditTrailEvents resultset");
                auditResultSet = ps.executeQuery();
                log.debug("Finished executing AuditTrailEvents query, it took: " + (System.currentTimeMillis() - tStart) + "ms");
            }
            if(auditResultSet.next()) {
                event = new AuditTrailEvent();
                event.setActionDateTime(CalendarUtils.getFromMillis(auditResultSet.getTimestamp(POSITION_OPERATION_DATE).getTime()));
                event.setActionOnFile(FileAction.fromValue(auditResultSet.getString(POSITION_OPERATION)));
                event.setAuditTrailInformation(auditResultSet.getString(POSITION_AUDIT_TRAIL));
                event.setActorOnFile(auditResultSet.getString(POSITION_ACTOR_NAME));
                event.setFileID(auditResultSet.getString(POSITION_FILE_ID));
                event.setInfo(auditResultSet.getString(POSITION_INFORMATION));
                event.setReportingComponent(auditResultSet.getString(POSITION_CONTRIBUTOR_ID));
                event.setSequenceNumber(BigInteger.valueOf(auditResultSet.getLong(POSITION_SEQUENCE_NUMBER)));
                event.setOperationID(auditResultSet.getString(POSITION_OPERATION_ID));
                event.setCertificateID(auditResultSet.getString(POSITION_FINGERPRINT));
            } else {
                close();
            }
    
            return event;
        } catch (Exception e) {
            try {
                close();
            } catch (SQLException e1) {
                throw new RuntimeException("Failed to close ResultSet or PreparedStatement", e1);
            }
            throw new IllegalStateException("Could not extract the wanted audittrails", e);
        } 
    }

}
