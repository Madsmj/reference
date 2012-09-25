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
package org.bitrepository.access.getfileids.conversation;

import java.net.URL;
import java.util.Collection;
import org.bitrepository.client.conversation.ConversationContext;
import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.protocol.messagebus.MessageSender;
import org.bitrepository.protocolversiondefinition.OperationType;

public class GetFileIDsConversationContext extends ConversationContext {
    private final URL urlForResult;

    public GetFileIDsConversationContext(String fileID, URL urlForResult,
            Settings settings, MessageSender messageSender, String clientID, Collection<String> contributors,
            EventHandler eventHandler) {
        super(OperationType.GET_FILE_I_DS, settings, messageSender, clientID, fileID, contributors, eventHandler, null);
        this.urlForResult = urlForResult;
    }

    public URL getUrlForResult() {
        return urlForResult;
    }

}
