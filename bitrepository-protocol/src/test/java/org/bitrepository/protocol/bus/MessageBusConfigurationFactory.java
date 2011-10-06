/*
 * #%L
 * Bitrepository Protocol
 * 
 * $Id$
 * $HeadURL$
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
package org.bitrepository.protocol.bus;

import org.bitrepository.settings.collectionsettings.MessageBusConfiguration;

/**
 * Consider moving definitions to disk
 */
public class MessageBusConfigurationFactory {
    
    private MessageBusConfigurationFactory() {}
    
    public static MessageBusConfiguration createDefaultConfiguration() {
        MessageBusConfiguration config = new MessageBusConfiguration();
        config.setURL("failover://tcp://sandkasse-01.kb.dk:61616");
        return config;
    }
    
    public static MessageBusConfiguration createEmbeddedMessageBusConfiguration() {
        MessageBusConfiguration config = new MessageBusConfiguration();
        config.setURL("tcp://localhost:61616");
        return config;
    }
}