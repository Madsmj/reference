/*
 * #%L
 * bitrepository-access-client
 * 
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2010 The State and University Library, The Royal Library and The State Archives, Denmark
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
package org.bitrepository.access.getfile;

import java.io.File;
import java.math.BigInteger;
import java.net.URL;

import org.bitrepository.access.AccessComponentFactory;
import org.bitrepository.bitrepositoryelements.TimeMeasureTYPE;
import org.bitrepository.bitrepositorymessages.GetFileFinalResponse;
import org.bitrepository.bitrepositorymessages.GetFileProgressResponse;
import org.bitrepository.bitrepositorymessages.GetFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileResponse;
import org.bitrepository.clienttest.DefaultFixtureClientTest;
import org.bitrepository.protocol.bitrepositorycollection.MutableClientSettings;
import org.bitrepository.protocol.fileexchange.TestFileStore;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test class for the 'GetFileClient'.
 */
public class GetFileClientComponentTest extends DefaultFixtureClientTest {
    private TestGetFileMessageFactory testMessageFactory;
    private TestFileStore pillar1FileStore;
    private TestFileStore pillar2FileStore;
    private MutableGetFileClientSettings getFileClientSettings;

    @BeforeMethod (alwaysRun=true)
    public void beforeMethodSetup() throws Exception {
        getFileClientSettings = new MutableGetFileClientSettings(settings);
        getFileClientSettings.setGetFileDefaultTimeout(1000);

        if (useMockupPillar()) {
            testMessageFactory = new TestGetFileMessageFactory(settings.getBitRepositoryCollectionID());
            pillar1FileStore = new TestFileStore("Pillar1");
            pillar2FileStore = new TestFileStore("Pillar2");
            // The following line is also relevant for non-mockup senarios, where the pillars needs to be initialized 
            // with content.
        }
        httpServer.clearFiles();
    }

    @Test(groups = {"regressiontest"})
    public void verifyGetFileClientFromFactory() throws Exception {
        Assert.assertTrue(AccessComponentFactory.getInstance().createGetFileClient(getFileClientSettings) 
                instanceof SimpleGetFileClient, 
                "The default GetFileClient from the Access factory should be of the type '" + 
                SimpleGetFileClient.class.getName() + "'.");
    }

    @Test(groups = {"regressiontest"})
    public void getFileFromSpecificPillar() throws Exception {
        addDescription("Tests whether a specific message is sent by the GetClient, when only a single pillar " +
        "participates");
        addStep("Set the number of pillars for this SLA to 1", "");

        ((MutableClientSettings)getFileClientSettings).setPillarIDs(new String[] {PILLAR1_ID});
        GetFileClient getFileClient = 
            new GetFileClientTestWrapper(AccessComponentFactory.getInstance().createGetFileClient(getFileClientSettings), 
                    testEventManager);

        addStep("Ensure the file isn't already present on the http server", "");
        httpServer.removeFile(DEFAULT_FILE_ID);

        addStep("Request the fastest delivery of a file. A callback listener should be supplied.", 
        "A IdentifyPillarsForGetFileRequest will be sent to the pillar.");
        getFileClient.getFileFromSpecificPillar(DEFAULT_FILE_ID, httpServer.getURL(DEFAULT_FILE_ID), PILLAR1_ID);
        IdentifyPillarsForGetFileRequest receivedIdentifyRequestMessage = null;
        if (useMockupPillar()) {
            receivedIdentifyRequestMessage = bitRepositoryCollectionDestination.waitForMessage(IdentifyPillarsForGetFileRequest.class);
            Assert.assertEquals(receivedIdentifyRequestMessage, 
                    testMessageFactory.createIdentifyPillarsForGetFileRequest(receivedIdentifyRequestMessage, 
                            bitRepositoryCollectionDestinationID));
        }

        addStep("The pillar sends a response to the identify message.", 
                "The callback listener should notify of the response and the client should send a GetFileRequest message to " +
        "the pillar"); 

        //Todo Verify the result when the GetFileClient callback interface has been defined 
        GetFileRequest receivedGetFileRequest = null;
        if (useMockupPillar()) {
            IdentifyPillarsForGetFileResponse identifyResponse = testMessageFactory.createIdentifyPillarsForGetFileResponse(
                    receivedIdentifyRequestMessage, PILLAR1_ID, pillar1DestinationId);
            messageBus.sendMessage(identifyResponse);
            receivedGetFileRequest = pillar1Destination.waitForMessage(GetFileRequest.class);
            Assert.assertEquals(receivedGetFileRequest, 
                    testMessageFactory.createGetFileRequest(receivedGetFileRequest,PILLAR1_ID, pillar1DestinationId));
        }

        addStep("The pillar sends a getFile response to the GetClient.", 
        "The GetClient should notify about the response through the callback interface."); 
        if (useMockupPillar()) {
            GetFileProgressResponse getFileProgressResponse = testMessageFactory.createGetFileProgressResponse(
                    receivedGetFileRequest, PILLAR1_ID, pillar1DestinationId);
            messageBus.sendMessage(getFileProgressResponse);
        }
        // ToDo The listener call should be verified.

        addStep("The file is uploaded to the indicated url and the pillar sends a final response upload message", 
                "The GetFileClient notifies that the file is ready through the callback listener and the uploaded " +
        "file is present.");
        if (useMockupPillar()) {
            // ToDo Switch to use test uploader using the attributes supplied in the request, eg. 
            // testPillar.uploadFile(receivedGetFileRequest.getFileAddress(), receivedGetFileRequest.getFileID());

            httpServer.uploadFile(pillar1FileStore.getInputstream(receivedGetFileRequest.getFileID()),
                    new URL(receivedGetFileRequest.getFileAddress()));

            GetFileFinalResponse completeMsg = testMessageFactory.createGetFileFinalResponse(
                    receivedGetFileRequest, PILLAR1_ID, pillar1DestinationId);
            messageBus.sendMessage(completeMsg);
        }
        // Todo Assert that the callback listener has received an 'uploadComplete' event.
        // How do we know the where the file is located. 
        File expectedUploadFile = pillar1FileStore.getFile(DEFAULT_FILE_ID);
        httpServer.assertFileEquals(expectedUploadFile, receivedGetFileRequest.getFileAddress());
    }

    @Test(groups = {"regressiontest"})
    public void chooseFastestPillarGetFileClient() throws Exception {
        addDescription("Set the GetClient to retrieve a file as fast as "
                + "possible, where it has to choose between to pillars with "
                + "different times. The messages should be delivered at the "
                + "same time.");
        addStep("Create a GetFileClient configured to use a fast and a slow pillar.", "");

        String averagePillarID = "THE-AVERAGE-PILLAR";
        String fastPillarID = "THE-FAST-PILLAR";
        String slowPillarID = "THE-SLOW-PILLAR";
        ((MutableClientSettings)getFileClientSettings).setPillarIDs(
                new String[] {averagePillarID, fastPillarID, slowPillarID});
        GetFileClient getFileClient = 
            new GetFileClientTestWrapper(AccessComponentFactory.getInstance().createGetFileClient(getFileClientSettings), 
                    testEventManager);

        addStep("Defining the variables for the GetFileClient and defining them in the configuration", 
        "It should be possible to change the values of the configurations.");

        addStep("Make the GetClient ask for fastest pillar.", 
        "It should send message to identify which pillars.");
        getFileClient.getFileFromFastestPillar(DEFAULT_FILE_ID, httpServer.getURL(DEFAULT_FILE_ID));
        IdentifyPillarsForGetFileRequest receivedIdentifyRequestMessage = null;
        if (useMockupPillar()) {
            receivedIdentifyRequestMessage = 
                bitRepositoryCollectionDestination.waitForMessage(IdentifyPillarsForGetFileRequest.class);
        }

        addStep("Three pillars send responses. First an average timeToDeliver, then a fast timeToDeliver and last a" +
                " slow timeToDeliver.", "The client should send a getFileRequest to the fast pillar");

        if (useMockupPillar()) {
            IdentifyPillarsForGetFileResponse averageReply = testMessageFactory.createIdentifyPillarsForGetFileResponse(
                    receivedIdentifyRequestMessage, averagePillarID, pillar2DestinationId);
            TimeMeasureTYPE averageTime = new TimeMeasureTYPE();
            averageTime.setTimeMeasureUnit("MILLISECONDS");
            averageTime.setTimeMeasureValue(BigInteger.valueOf(100L));
            averageReply.setTimeToDeliver(averageTime);
            messageBus.sendMessage(averageReply);

            IdentifyPillarsForGetFileResponse fastReply = testMessageFactory.createIdentifyPillarsForGetFileResponse(
                    receivedIdentifyRequestMessage, fastPillarID, pillar1DestinationId);
            TimeMeasureTYPE fastTime = new TimeMeasureTYPE();
            fastTime.setTimeMeasureUnit("MILLISECONDS");
            fastTime.setTimeMeasureValue(BigInteger.valueOf(10L));
            fastReply.setTimeToDeliver(fastTime);
            messageBus.sendMessage(fastReply);

            IdentifyPillarsForGetFileResponse slowReply = testMessageFactory.createIdentifyPillarsForGetFileResponse(
                    receivedIdentifyRequestMessage, slowPillarID, pillar2DestinationId);
            TimeMeasureTYPE slowTime = new TimeMeasureTYPE();
            slowTime.setTimeMeasureValue(BigInteger.valueOf(1L));
            slowTime.setTimeMeasureUnit("HOURS");
            slowReply.setTimeToDeliver(slowTime);
            messageBus.sendMessage(slowReply);

            GetFileRequest receivedGetFileRequest = pillar1Destination.waitForMessage(GetFileRequest.class);
            Assert.assertEquals(receivedGetFileRequest, 
                    testMessageFactory.createGetFileRequest(receivedGetFileRequest, fastPillarID, pillar1DestinationId));
        }
    }
}
