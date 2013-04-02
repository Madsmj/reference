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
package org.bitrepository.pillar.integration.func;

import java.util.Arrays;
import java.util.Collection;

import org.bitrepository.common.exceptions.OperationFailedException;
import org.bitrepository.common.utils.TestFileHelper;
import org.bitrepository.pillar.integration.PillarIntegrationTest;
import org.bitrepository.protocol.bus.MessageReceiver;
import org.testng.ITestContext;
import org.testng.annotations.BeforeSuite;

/**
 * The parent class for pillar acceptance tests. The tests can be run in a multi pillar collection has the tests will
 * ignore responses from other pillars.
 */
public abstract class PillarFunctionTest extends PillarIntegrationTest {
    /** Used for receiving responses from the pillar */
    protected MessageReceiver clientReceiver;

    @BeforeSuite(alwaysRun = true)
    @Override
    public void initializeSuite(ITestContext testContext) {
        super.initializeSuite(testContext);
        putDefaultFile();
    }

    @Override
    protected void registerMessageReceivers() {
        super.registerMessageReceivers();

        clientReceiver = new MessageReceiver(settingsForTestClient.getReceiverDestinationID(), testEventManager);
        addReceiver(clientReceiver);

        Collection<String> pillarFilter = Arrays.asList(testConfiguration.getPillarUnderTestID());
        clientReceiver.setFromFilter(pillarFilter);
        alarmReceiver.setFromFilter(pillarFilter);
    }

    protected void putDefaultFile() {
        try {
            clientProvider.getPutClient().putFile(
                    collectionID, DEFAULT_FILE_URL, DEFAULT_FILE_ID, 10L, TestFileHelper.getDefaultFileChecksum(),
                null, null, null);
        } catch (OperationFailedException e) {
            throw new RuntimeException(e);
        }
    }
}
