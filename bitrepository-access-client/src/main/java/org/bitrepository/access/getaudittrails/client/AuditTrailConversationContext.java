/*
 * #%L
 * Bitrepository Access
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
package org.bitrepository.access.getaudittrails.client;

import org.bitrepository.access.getaudittrails.AuditTrailQuery;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.protocol.conversation.ConversationContext;
import org.bitrepository.protocol.eventhandler.EventHandler;
import org.bitrepository.protocol.messagebus.MessageSender;

public class AuditTrailConversationContext extends ConversationContext {
    private final AuditTrailQuery[] componentQueries;
    private final String fileID;
    private final String urlForResult;
    private final String clientID;

    public AuditTrailConversationContext(AuditTrailQuery[] componentQueries, String fileID, String urlForResult,
            Settings settings, MessageSender messageSender, String clientID, EventHandler eventHandler,
            String auditTrailInformation) {
        super(settings, messageSender, eventHandler, auditTrailInformation);
        this.componentQueries = componentQueries;
        this.fileID = fileID;
        this.urlForResult = urlForResult;
        this.clientID = clientID;
        
    }

    public AuditTrailQuery[] getComponentQueries() {
        return componentQueries;
    }

    public String getFileID() {
        return fileID;
    }

    public String getUrlForResult() {
        return urlForResult;
    }
    
    public String getClientID() {
        return clientID;
    }
}
