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
package org.bitrepository.protocol;

import org.bitrepository.common.ConfigurationFactory;
import org.bitrepository.common.ModuleCharacteristics;
import org.bitrepository.protocol.activemq.ActiveMQMessageBus;
import org.bitrepository.protocol.bitrepositorycollection.CollectionSettings;
import org.bitrepository.protocol.configuration.FileExchangeConfiguration;
import org.bitrepository.protocol.configuration.ProtocolConfiguration;
import org.bitrepository.protocol.messagebus.MessageBus;

/**
 * Provides access to the different component in the protocol module (Spring/IOC wannabe)
 */
public class ProtocolComponentFactory {

    /** The singleton instance */
    protected static ProtocolComponentFactory instance;

    /**
     * The singletonic access to the instance of this class
     * @return The one and only instance
     */
    public static synchronized ProtocolComponentFactory getInstance() {
        if (instance == null) {
            instance = new ProtocolComponentFactory();
        }
        return instance;
    }

    /**
     * The singleton constructor.
     */
    protected ProtocolComponentFactory() {
    }

    /** @see #getModuleCharacteristics() */
    private static final ModuleCharacteristics MODULE_CHARACTERISTICS = new ModuleCharacteristics("protocol");
    /** @see #getProtocolConfiguration() */
    protected ProtocolConfiguration protocolConfiguration;
    /** @see #getMessageBus() */
    protected MessageBus messagebus;
    /** @see #getFileExchange() */
    protected FileExchange fileExchange;

    /**
     * Gets you a <code>ModuleCharacteristics</code> object defining the generic characteristics of this module
     * @return A <code>ModuleCharacteristics</code> object defining the generic characteristics of this module 
     */
    public ModuleCharacteristics getModuleCharacteristics() {
        return MODULE_CHARACTERISTICS;
    }

    /**
     * Gets you an <code>MessageBus</code> instance for accessing the Bitrepositorys message bus.
     * @return The messagebus for this collection
     */
    public MessageBus getMessageBus(CollectionSettings settings) {
        if (messagebus == null) {
            messagebus = new ActiveMQMessageBus(settings.getStandardSettings().getMessageBusConfiguration());
        }
        return messagebus;
    }

    /**
     * Gets you an <code>FileExchange</code> instance for data communication. Is instantiated based on the 
     * configurations.
     * @return The FileExchange according to the configuration.
     * @deprecated The file exchange class will be replaced by the HttpServerConnector.
     */
    public FileExchange getFileExchange() {
        if (fileExchange == null) {
            try {
                FileExchangeConfiguration feConf = getProtocolConfiguration().getFileExchangeConfigurations();
                Class instantiation = Class.forName(feConf.getFileExchangeClass());
                fileExchange = (FileExchange) instantiation.newInstance();
            } catch (Exception e) {
                throw new CoordinationLayerException("Could not instantiate the fileexchange.", e);
            }
        }
        return fileExchange;
    }

    /**
     * Gets you the configuration for this module. The configuration object is loaded from file the first time this
     * method is called, and cannot be reloaded.
     * @return Gets you the configuration for this module
     */
    public ProtocolConfiguration getProtocolConfiguration() {
        if (protocolConfiguration == null) {
            ConfigurationFactory configurationFactory = new ConfigurationFactory();
            protocolConfiguration =
                configurationFactory.loadConfiguration(getModuleCharacteristics(), ProtocolConfiguration.class);
        }
        return protocolConfiguration;
    }
}
