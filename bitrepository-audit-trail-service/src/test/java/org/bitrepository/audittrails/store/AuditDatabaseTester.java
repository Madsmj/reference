/*
 * #%L
 * Bitrepository Audit Trail Service
 * %%
 * Copyright (C) 2010 - 2012 The State and University Library, The Royal Library and The State Archives, Denmark
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */
package org.bitrepository.audittrails.store;

import java.math.BigInteger;
import java.sql.Connection;
import java.util.Date;
import java.util.List;

import org.bitrepository.bitrepositoryelements.AuditTrailEvent;
import org.bitrepository.bitrepositoryelements.AuditTrailEvents;
import org.bitrepository.bitrepositoryelements.FileAction;
import org.bitrepository.common.database.DatabaseUtils;
import org.bitrepository.common.database.DerbyDBConnector;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.TestSettingsProvider;
import org.bitrepository.common.utils.CalendarUtils;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class AuditDatabaseTester extends ExtendedTestCase {
    /** The settings for the tests. Should be instantiated in the setup.*/
    Settings settings;
    String DATABASE_URL = "jdbc:derby:auditservicedb";
    String fileId = "TEST-FILE-ID-" + new Date().getTime();
    String fileId2 = "ANOTHER-FILE-ID" + new Date().getTime();;
    String pillarId = "MY-TEST-PILLAR";
    String actor1 = "ACTOR-1";
    String actor2 = "ACTOR-2";
    
    @BeforeClass (alwaysRun = true)
    public void setup() throws Exception {
        settings = TestSettingsProvider.reloadSettings();
    }
    
    @Test(groups = {"databasetest"})
    public void AuditDatabaseExtractionTest() throws Exception {
        addDescription("Testing the connection to the audit trail service database especially with regards to "
                + "extracting the data from it.");
        addStep("Setup the variables and constants.", "Should be ok.");
        Date restrictionDate = new Date(123456789); // Sometime between epoch and now!
        
        addStep("Adds the variables to the settings and instantaites the database cache", "Should be connected.");
        settings.getReferenceSettings().getIntegrityServiceSettings().setDatabaseUrl(DATABASE_URL);
        AuditTrailServiceDAO database = new AuditTrailServiceDAO(settings);
        clearDatabase(DATABASE_URL);

        addStep("Validate that the database is empty and then populate it.", "Should be possible.");
        Assert.assertEquals(database.largestSequenceNumber(pillarId), 0);
        database.addAuditTrails(createEvents());
        
        addStep("", "");
        Assert.assertEquals(database.largestSequenceNumber(pillarId), 10);
        List<AuditTrailEvent> res = database.getAuditTrails(null, null, null, null, null, null, null, null);
        Assert.assertEquals(res.size(), 2, res.toString());
        
        addStep("Test the extraction of FileID", "Should be able to extract the audit of each file individually.");
        res = database.getAuditTrails(fileId, null, null, null, null, null, null, null);
        Assert.assertEquals(res.size(), 1, res.toString());
        Assert.assertEquals(res.get(0).getFileID(), fileId);
        
        res = database.getAuditTrails(fileId2, null, null, null, null, null, null, null);
        Assert.assertEquals(res.size(), 1, res.toString());
        Assert.assertEquals(res.get(0).getFileID(), fileId2);
        
        addStep("Perform extraction based on the component id.", "");
        res = database.getAuditTrails(null, pillarId, null, null, null, null, null, null);
        Assert.assertEquals(res.size(), 2, res.toString());
        res = database.getAuditTrails(null, "NO COMPONENT", null, null, null, null, null, null);
        Assert.assertEquals(res.size(), 0, res.toString());
        
        addStep("Perform extraction based on the sequence number restriction", 
                "Should be possible to have both lower and upper sequence number restrictions.");
        res = database.getAuditTrails(null, null, 5L, null, null, null, null, null);
        Assert.assertEquals(res.size(), 1, res.toString());
        Assert.assertEquals(res.get(0).getFileID(), fileId2);
        res = database.getAuditTrails(null, null, null, 5L, null, null, null, null);
        Assert.assertEquals(res.size(), 1, res.toString());
        Assert.assertEquals(res.get(0).getFileID(), fileId);
        
        addStep("Perform extraction based on actor id restriction.", 
                "Should be possible to restrict on the id of the actor.");
        res = database.getAuditTrails(null, null, null, null, actor1, null, null, null);
        Assert.assertEquals(res.size(), 1, res.toString());
        Assert.assertEquals(res.get(0).getActorOnFile(), actor1);
        res = database.getAuditTrails(null, null, null, null, actor2, null, null, null);
        Assert.assertEquals(res.size(), 1, res.toString());
        Assert.assertEquals(res.get(0).getActorOnFile(), actor2);
        
        addStep("Perform extraction based on operation restriction.", 
                "Should be possible to restrict on the FileAction operation.");
        res = database.getAuditTrails(null, null, null, null, null, FileAction.INCONSISTENCY, null, null);
        Assert.assertEquals(res.size(), 1, res.toString());
        Assert.assertEquals(res.get(0).getActionOnFile(), FileAction.INCONSISTENCY);
        res = database.getAuditTrails(null, null, null, null, null, FileAction.FAILURE, null, null);
        Assert.assertEquals(res.size(), 1, res.toString());
        Assert.assertEquals(res.get(0).getActionOnFile(), FileAction.FAILURE);
        
        addStep("Perform extraction based on date restriction.", 
                "Should be possible to restrict on the date of the audit.");
        res = database.getAuditTrails(null, null, null, null, null, null, restrictionDate, null);
        Assert.assertEquals(res.size(), 1, res.toString());
        Assert.assertEquals(res.get(0).getFileID(), fileId2);
        res = database.getAuditTrails(null, null, null, null, null, null, null, restrictionDate);
        Assert.assertEquals(res.size(), 1, res.toString());
        Assert.assertEquals(res.get(0).getFileID(), fileId);
    }
    
    
    private AuditTrailEvents createEvents() {
        AuditTrailEvents events = new AuditTrailEvents();
        
        AuditTrailEvent event1 = new AuditTrailEvent();
        event1.setActionDateTime(CalendarUtils.getEpoch());
        event1.setActionOnFile(FileAction.INCONSISTENCY);
        event1.setActorOnFile(actor1);
        event1.setAuditTrailInformation("I AM AUDIT");
        event1.setFileID(fileId);
        event1.setInfo(null);
        event1.setReportingComponent(pillarId);
        event1.setSequenceNumber(BigInteger.ONE);
        events.getAuditTrailEvent().add(event1);
        
        AuditTrailEvent event2 = new AuditTrailEvent();
        event2.setActionDateTime(CalendarUtils.getNow());
        event2.setActionOnFile(FileAction.FAILURE);
        event2.setActorOnFile(actor2);
        event2.setAuditTrailInformation(null);
        event2.setFileID(fileId2);
        event2.setInfo("WHAT AM I DOING?");
        event2.setReportingComponent(pillarId);
        event2.setSequenceNumber(BigInteger.TEN);
        events.getAuditTrailEvent().add(event2);

        return events;
    }

    private void clearDatabase(String url) throws Exception {
        Connection con = new DerbyDBConnector().getEmbeddedDBConnection(url);
        
        String sqlFI = "DELETE FROM " + AuditDatabaseConstants.FILE_TABLE;
        DatabaseUtils.executeStatement(con, sqlFI, new Object[0]);
        String sqlFiles = "DELETE FROM " + AuditDatabaseConstants.CONTRIBUTOR_TABLE;
        DatabaseUtils.executeStatement(con, sqlFiles, new Object[0]);
        String sqlPillar = "DELETE FROM " + AuditDatabaseConstants.AUDITTRAIL_TABLE;
        DatabaseUtils.executeStatement(con, sqlPillar, new Object[0]);
        String sqlCs = "DELETE FROM " + AuditDatabaseConstants.ACTOR_TABLE;
        DatabaseUtils.executeStatement(con, sqlCs, new Object[0]);
    }
}