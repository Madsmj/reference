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
package org.bitrepository.pillar.checksumpillar;

import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.SettingsProvider;
import org.bitrepository.common.settings.XMLFileSettingsLoader;
import org.bitrepository.pillar.PillarComponentFactory;
import org.bitrepository.protocol.messagebus.MessageBusManager;
import org.bitrepository.protocol.security.BasicMessageAuthenticator;
import org.bitrepository.protocol.security.BasicMessageSigner;
import org.bitrepository.protocol.security.BasicOperationAuthorizor;
import org.bitrepository.protocol.security.BasicSecurityManager;
import org.bitrepository.protocol.security.MessageAuthenticator;
import org.bitrepository.protocol.security.MessageSigner;
import org.bitrepository.protocol.security.OperationAuthorizor;
import org.bitrepository.protocol.security.PermissionStore;
import org.bitrepository.protocol.security.SecurityManager;

/**
 * Method for launching the ChecksumPillar. 
 * It just loads the settings from the given path, initiates the messagebus (with security) and then starts the 
 * ChecksumPillar.
 */
public class ChecksumPillarLauncher {
    /** The default path to the collection id during development.*/
    private static final String DEFAULT_COLLECTION_ID = "bitrepository-devel";
    /** The default path for the settings in the development.*/
    private static final String DEFAULT_PATH_TO_SETTINGS = "settings/xml";
    /** The default path for the settings in the development.*/
    private static final String DEFAULT_PATH_TO_KEY_FILE = "conf/client.p12";
    
    /**
     * Private constructor. To prevent instantiation of this utility class.
     */
    private ChecksumPillarLauncher() { }
    
    /**
     * @param args <ol>
     * <li> The path to the directory containing the settings. See {@link XMLFileSettingsLoader} for details.</li>
     * <li> The path to the private key file with the certificates for communication.</li>
     * <li> The collection ID to load the settings for.</li>
     * </ol>
     */
    public static void main(String[] args) {
        String collectionId = DEFAULT_COLLECTION_ID;
        String pathToSettings = DEFAULT_PATH_TO_SETTINGS;
        String privateKeyFile = DEFAULT_PATH_TO_KEY_FILE;
        if(args.length >= 3) {
            pathToSettings = args[0];
            privateKeyFile = args[1];
            collectionId = args[2];
        } else if(args.length == 2) {
            pathToSettings = args[0];
            privateKeyFile = args[1];
            collectionId = ".";
        } else if(args.length == 1) {
            pathToSettings = args[0];
            collectionId = ".";
        }
        
        // Instantiate the settings for the ChecksumPillar.
        Settings settings = null;
        try {
            SettingsProvider settingsLoader = new SettingsProvider(
                    new XMLFileSettingsLoader(pathToSettings));
            settings = settingsLoader.getSettings(collectionId);
        } catch (Exception e) {
            System.err.println("Could not load the settings from '" + pathToSettings + "'.");
            e.printStackTrace();
            System.exit(-1);
        }
        
        // Instantiate the security for the messagebus for the ChecksumPillar.
        SecurityManager securityManager = null;
        try {
            PermissionStore permissionStore = new PermissionStore();
            MessageAuthenticator authenticator = new BasicMessageAuthenticator(permissionStore);
            MessageSigner signer = new BasicMessageSigner();
            OperationAuthorizor authorizer = new BasicOperationAuthorizor(permissionStore);
            securityManager = new BasicSecurityManager(settings.getCollectionSettings(), privateKeyFile, 
                    authenticator, signer, authorizer, permissionStore);
        } catch (Exception e) {
            System.err.println("Could not instantiate the security manager.");
            e.printStackTrace();
            System.exit(-1);
        }
        
        try {
            PillarComponentFactory.getInstance().getChecksumPillar(MessageBusManager.getMessageBus(settings, 
                    securityManager), settings);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }   
}
