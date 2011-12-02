/*
 * #%L
 * Bitrepository Access Client
 * 
 * $Id: PutClientTestWrapper.java 575 2011-11-29 13:15:38Z ktc $
 * $HeadURL: https://sbforge.org/svn/bitrepository/bitrepository-reference/trunk/bitrepository-modifying-client/src/test/java/org/bitrepository/modify/putfile/PutClientTestWrapper.java $
 * %%
 * Copyright (C) 2010 - 2011 The State and University Library, The Royal Library and The State Archives, Denmark
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
package org.bitrepository.modify.deletefile;

import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.protocol.eventhandler.EventHandler;
import org.bitrepository.protocol.exceptions.OperationFailedException;
import org.jaccept.TestEventManager;

/**
 * Wrapper class for a PutFileClient.
 */
public class DeleteClientTestWrapper implements DeleteFileClient {
    /** The PutClient to wrap. */
    private DeleteFileClient wrappedDeleteClient;
    /** The manager to monitor the operations.*/
    private TestEventManager testEventManager;

    /**
     * Constructor.
     * @param putClientInstance The instance to wrap and monitor.
     * @param eventManager The manager to monitor the operations.
     */
    public DeleteClientTestWrapper(DeleteFileClient deleteClientInstance, TestEventManager eventManager) {
        this.wrappedDeleteClient = deleteClientInstance;
        this.testEventManager = eventManager;
    }

    @Override
    public void deleteFile(String fileId, String pillarId, String checksum, ChecksumSpecTYPE checksumType,
            EventHandler eventHandler, String auditTrailInformation) throws OperationFailedException {
        testEventManager.addStimuli("Calling deleteFile(" + fileId + ", " + pillarId + ", " + checksum + ", " 
                + checksumType + ", eventHandler, " + auditTrailInformation + ")");
        wrappedDeleteClient.deleteFile(fileId, pillarId, checksum, checksumType, eventHandler, auditTrailInformation);        
    }
    
    @Override
    public void shutdown() {
        testEventManager.addStimuli("Calling shutdown()");
        wrappedDeleteClient.shutdown();
    }
}
