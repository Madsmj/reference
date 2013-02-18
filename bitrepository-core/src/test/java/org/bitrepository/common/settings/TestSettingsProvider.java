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
package org.bitrepository.common.settings;


import java.util.HashMap;
import java.util.Map;
import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.settings.repositorysettings.Collection;

/**
 * Helper class for easy access to the settings located in the <code>settings/xml</code> dir.
 */
public class TestSettingsProvider {
    private static final Map<String, SettingsProvider> settingsproviders = new HashMap<String, SettingsProvider>();

    private static final String SETTINGS_LOCATION = "settings/xml/bitrepository-devel";
    
    private static final String DEFAULT_PILLAR_ID_TO_REPLACE = "Pillar1";

    /** 
     * Returns the settings for the collection defined by the COLLECTIONID_PROPERTY system variable if defined. If 
     * undefined the DEVELOPMENT_ENVIRONMENT settings will be loaded.
     */
    public static Settings getSettings(String componentID) {
        return getSettingsProvider(componentID).getSettings();
    }
    
    /** 
     * Reloads the settings from disk.
     */
    public static Settings reloadSettings(String componentID) {
        getSettingsProvider(componentID).reloadSettings();
        return getSettings(componentID);
    }
    
    /**
     * Reloads the settings for the pillar, and replaces the default pillar in the collections with the given pillar id.
     * @param pillarID The id of the pillar to reload the settings for.
     * @return The settings for the pillar.
     */
    public static Settings reloadSettingsForPillar(String pillarID) {
        Settings res = reloadSettings(pillarID);
        for(Collection c : res.getRepositorySettings().getCollections().getCollection()) {
            if(c.getPillarIDs().getPillarID().remove(DEFAULT_PILLAR_ID_TO_REPLACE)) {
                c.getPillarIDs().getPillarID().add(pillarID);
            }
        }
        return res;
    }

    private static SettingsProvider getSettingsProvider(String componentID) {
        ArgumentValidator.checkNotNull(componentID, "componentID");
        if (!settingsproviders.containsKey(componentID)) {
            settingsproviders.put(componentID,
                    new SettingsProvider(new XMLFileSettingsLoader(SETTINGS_LOCATION),
                            componentID));
        }

        return settingsproviders.get(componentID);
    }
}
