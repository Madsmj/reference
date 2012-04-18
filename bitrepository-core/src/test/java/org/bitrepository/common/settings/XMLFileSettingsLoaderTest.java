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

import org.bitrepository.settings.collectionsettings.CollectionSettings;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;

public class XMLFileSettingsLoaderTest extends ExtendedTestCase{
    private static final String COLLECTION_ID = "bitrepository-devel";
    private static final String PATH_TO_SETTINGS = "settings/xml/bitrepository-devel";
    
    @Test(groups = { "regressiontest" })
    public void testCollectionSettingsLoading() throws Exception {
        SettingsLoader settingsLoader = new XMLFileSettingsLoader(PATH_TO_SETTINGS);
        
        CollectionSettings collectionSettings = settingsLoader.loadSettings(CollectionSettings.class);
        Assert.assertNotNull(collectionSettings, "CollectionSettings");
    }
}