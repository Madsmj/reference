/*
 * #%L
 * Bitrepository Common
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
package org.bitrepository.common.settings;

import java.util.ArrayList;
import java.util.List;
import org.bitrepository.settings.referencesettings.ReferenceSettings;
import org.bitrepository.settings.repositorysettings.Collection;
import org.bitrepository.settings.repositorysettings.MessageBusConfiguration;
import org.bitrepository.settings.repositorysettings.Permission;
import org.bitrepository.settings.repositorysettings.RepositorySettings;

/**
 * Contains the general configuration to be used by reference code components. Provides acces to both
 * {@link ReferenceSettings} and {@link org.bitrepository.settings.repositorysettings.RepositorySettings}. Use a {@link SettingsProvider} to access settings create
 * <code>Settings</code> objects.
 */
public class Settings {
    protected final String componentID;
    protected final String receiverDestinationID;
    protected final ReferenceSettings referenceSettings;
    protected final RepositorySettings repositorySettings;
    
    protected Settings(
            String componentID,
            String receiverDestinationID,
            RepositorySettings repositorySettings,
            ReferenceSettings referenceSettings) {
        this.componentID = componentID;
        this.receiverDestinationID = receiverDestinationID;

        this.referenceSettings = referenceSettings;
        this.repositorySettings = repositorySettings;
    }

    /**
     * @return the first Collections ID.
     */
    public List<Collection> getCollections() {
        return getRepositorySettings().getCollections().getCollection();
    }
    
    /**
     * @return The first collection defined in the repository settings.
     */
    public Collection getCollection() {
        return getRepositorySettings().getCollections().getCollection().get(0);
    }
    
    /**
     * @return The list of collection ids, which this component is part of.
     */
    public List<String> getMyCollectionIDs() {
        List<String> res = new ArrayList<String>();
        for(Collection c : getRepositorySettings().getCollections().getCollection()) {
            if(c.getPillarIDs().getPillarID().contains(componentID)) {
                res.add(c.getID());
            }
        }
        
        return res;
    }
    
    /**
     * Wraps the {@link org.bitrepository.settings.repositorysettings.ProtocolSettings#getAlarmDestination()} method.
     */
    public String getAlarmDestination() {
        return getRepositorySettings().getProtocolSettings().getAlarmDestination();
    }
    
    /**
     * Wraps the {@link org.bitrepository.settings.repositorysettings.ProtocolSettings#getCollectionDestination()} method.
     */
    public String getCollectionDestination() {
        return getRepositorySettings().getProtocolSettings().getCollectionDestination();
    }
    
    /**
     * Wraps the {@link org.bitrepository.settings.repositorysettings.ProtocolSettings#getMessageBusConfiguration()} method.
     */
    public MessageBusConfiguration getMessageBusConfiguration() {
        return getRepositorySettings().getProtocolSettings().getMessageBusConfiguration();
    }
    
    /**
     * Wraps the {@link org.bitrepository.settings.repositorysettings.PermissionSet#getPermission()}} method.
     */
    public List<Permission> getPermissions() {
        return getRepositorySettings().getPermissionSet().getPermission();
    }
    
    /**
     * Wraps the {@link org.bitrepository.settings.repositorysettings.ClientSettings#getIdentificationTimeout()} method.
     */
    public long getIdentificationTimeout() {
        return getRepositorySettings().getClientSettings().getIdentificationTimeout().longValue();
    }
      
    /**
     * @return The settings specific to the reference code for a collection.
     */
    public ReferenceSettings getReferenceSettings() {
        return referenceSettings;
    }
    
    /**
     * @return The standard settings for a collection.
     */
    public RepositorySettings getRepositorySettings() {
        return repositorySettings;
    }

    public String getComponentID() {
        return componentID;
    }

    public String getReceiverDestinationID() {
        return receiverDestinationID;
    }

    public String getContributorDestinationID() {
        return receiverDestinationID + "-contributor";
    }
}
