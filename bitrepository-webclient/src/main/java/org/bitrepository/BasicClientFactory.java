/*
 * #%L
 * Bitrepository Webclient
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
package org.bitrepository;

import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.SettingsProvider;
import org.bitrepository.common.settings.XMLFileSettingsLoader;
import org.bitrepository.protocol.security.BasicMessageAuthenticator;
import org.bitrepository.protocol.security.BasicMessageSigner;
import org.bitrepository.protocol.security.BasicOperationAuthorizor;
import org.bitrepository.protocol.security.BasicSecurityManager;
import org.bitrepository.protocol.security.MessageAuthenticator;
import org.bitrepository.protocol.security.MessageSigner;
import org.bitrepository.protocol.security.OperationAuthorizor;
import org.bitrepository.protocol.security.PermissionStore;
import org.bitrepository.protocol.security.SecurityManager;
import org.bitrepository.webservice.ServiceUrl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class BasicClientFactory {
    private static final Logger log = LoggerFactory.getLogger(BasicClientFactory.class);
    private static BasicClient client;
    private static String confDir; 
    private static String logFile;
    private static String privateKeyFile;
    private static String clientID;
    private static final String CONFIGFILE = "webclient.properties"; 
    private static final String LOGFILE = "org.bitrepository.webclient.logfile";
    private static final String PRIVATE_KEY_FILE = "org.bitrepository.webclient.privateKeyFile";
    private static final String CLIENT_ID = "org.bitrepository.webclient.clientID";


    /**
     * Set the configuration directory. 
     * Should only be run at initialization time. 
     */
    public synchronized static void init(String configurationDir) {
        confDir = configurationDir;
        ServiceUrl.init(configurationDir);
        loadProperties();
    }

    /**
     *	Factory method to get a singleton instance of BasicClient
     *	@return The BasicClient instance or a null in case of trouble.  
     */
    public synchronized static BasicClient getInstance() {
        if(client == null) {
            if(confDir == null) {
                throw new RuntimeException("No configuration dir has been set!");
            }
            SettingsProvider settingsLoader = new SettingsProvider(new XMLFileSettingsLoader(confDir), clientID);
            Settings settings = settingsLoader.getSettings();
            PermissionStore permissionStore = new PermissionStore();
            MessageAuthenticator authenticator = new BasicMessageAuthenticator(permissionStore);
            MessageSigner signer = new BasicMessageSigner();
            OperationAuthorizor authorizer = new BasicOperationAuthorizor(permissionStore);
            SecurityManager securityManager = new BasicSecurityManager(settings.getCollectionSettings(), privateKeyFile,
                    authenticator, signer, authorizer, permissionStore, clientID);

            try {
                client = new BasicClient(settings, securityManager, logFile, clientID);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                throw new RuntimeException(e);
            }
        }
        return client;

    } 
    /**
     * Load properties from configuration file 
     */
    private static void loadProperties() {
        Properties properties = new Properties();
        try {
            String propertiesFile = confDir + "/" + CONFIGFILE;
            BufferedReader reader = new BufferedReader(new FileReader(propertiesFile));
            properties.load(reader);

            logFile = properties.getProperty(LOGFILE);
            privateKeyFile = properties.getProperty(PRIVATE_KEY_FILE);
            clientID = properties.getProperty(CLIENT_ID);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
