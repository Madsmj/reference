/*
 * #%L
 * Bitrepository Reference Pillar
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
package org.bitrepository.pillar.referencepillar;

import java.io.File;

import org.bitrepository.bitrepositorymessages.DeleteFileRequest;
import org.bitrepository.bitrepositorymessages.GetAuditTrailsRequest;
import org.bitrepository.bitrepositorymessages.GetChecksumsRequest;
import org.bitrepository.bitrepositorymessages.GetFileIDsRequest;
import org.bitrepository.bitrepositorymessages.GetFileRequest;
import org.bitrepository.bitrepositorymessages.GetStatusRequest;
import org.bitrepository.bitrepositorymessages.IdentifyContributorsForGetStatusRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForDeleteFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetChecksumsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileIDsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForReplaceFileRequest;
import org.bitrepository.bitrepositorymessages.Message;
import org.bitrepository.bitrepositorymessages.PutFileRequest;
import org.bitrepository.bitrepositorymessages.ReplaceFileRequest;
import org.bitrepository.common.utils.FileUtils;
import org.bitrepository.pillar.DefaultFixturePillarTest;
import org.bitrepository.pillar.MockAlarmDispatcher;
import org.bitrepository.pillar.MockAuditManager;
import org.bitrepository.pillar.common.PillarContext;
import org.bitrepository.pillar.referencepillar.messagehandler.ReferencePillarMediator;
import org.bitrepository.settings.collectionsettings.AlarmLevel;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ReferencePillarMediatorTester extends DefaultFixturePillarTest {
    
    ReferenceArchive archive;
    ReferencePillarMediator mediator;
    MockAlarmDispatcher alarmDispatcher;
    MockAuditManager audits;
    
    @BeforeMethod (alwaysRun=true)
    public void initialiseGetChecksumsTests() throws Exception {
        File dir = new File(settings.getReferenceSettings().getPillarSettings().getFileDir());
        settings.getCollectionSettings().getPillarSettings().setAlarmLevel(AlarmLevel.WARNING);
        if(dir.exists()) {
            FileUtils.delete(dir);
        }
        
        addStep("Initialize the pillar.", "Should not be a problem.");
        archive = new ReferenceArchive(settings.getReferenceSettings().getPillarSettings().getFileDir());
        audits = new MockAuditManager();
        alarmDispatcher = new MockAlarmDispatcher(settings, messageBus);
        PillarContext context = new PillarContext(settings, messageBus, alarmDispatcher, audits);
        mediator = new ReferencePillarMediator(context, archive);
    }
    
    @AfterMethod (alwaysRun=true) 
    public void closeArchive() {
        File dir = new File(settings.getReferenceSettings().getPillarSettings().getFileDir());
        if(dir.exists()) {
            FileUtils.delete(dir);
        }
        
        if(mediator != null) {
            mediator.close();
        }
    }
    
    @Test( groups = {"regressiontest", "pillartest"})
    public void handleMessagesWithNoHandlers() throws Exception {
        addDescription("Test the mediators handling of messages, where it does not have any handler.");
        addStep("Clean up the mediator.", "The mediator should no longer contain any message handler.");
        mediator.close();
        
        testMessage(new Message());
        testMessage(new DeleteFileRequest());
        testMessage(new GetAuditTrailsRequest());
        testMessage(new GetChecksumsRequest());
        testMessage(new GetFileIDsRequest());
        testMessage(new GetFileRequest());
        testMessage(new GetStatusRequest());
        testMessage(new IdentifyContributorsForGetStatusRequest());
        testMessage(new IdentifyPillarsForDeleteFileRequest());
        testMessage(new IdentifyPillarsForGetChecksumsRequest());
        testMessage(new IdentifyPillarsForGetFileIDsRequest());
        testMessage(new IdentifyPillarsForGetFileRequest());
        testMessage(new IdentifyPillarsForPutFileRequest());
        testMessage(new IdentifyPillarsForReplaceFileRequest());
        testMessage(new PutFileRequest());
        testMessage(new ReplaceFileRequest());
    }
    
    private void testMessage(Message message) {
        addStep("Testing '" + message + "'.", "Should send an alarm for failed operation.");
        alarmDispatcher.resetCallsForSendAlarm();
        audits.resetCallsForAuditEvent();
        mediator.onMessage(message);
        Assert.assertEquals(alarmDispatcher.getCallsForSendAlarm(), 1);
        Assert.assertEquals(audits.getCallsForAuditEvent(), 0);
    }
    
    @Test( groups = {"regressiontest", "pillartest"})
    public void handleMessagesWithNoAlarms() throws Exception {
        addDescription("Tests that if the alarm level is too high, then the alarms are not send for the Message.");
        addStep("Setup variables, e.g. changing the alarm level.", "Should be ok.");
        settings.getCollectionSettings().getPillarSettings().setAlarmLevel(AlarmLevel.EMERGENCY);
        
        alarmDispatcher.resetCallsForSendAlarm();
        audits.resetCallsForAuditEvent();
        mediator.onMessage(new Message());
        Assert.assertEquals(alarmDispatcher.getCallsForSendAlarm(), 0);
        Assert.assertEquals(audits.getCallsForAuditEvent(), 0);
    }
}
