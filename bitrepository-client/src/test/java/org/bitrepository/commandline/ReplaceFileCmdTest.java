/*
 * #%L
 * Bitrepository Command Line
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
package org.bitrepository.commandline;

import java.util.Date;

import org.bitrepository.client.DefaultFixtureClientTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ReplaceFileCmdTest extends DefaultFixtureClientTest {
    private static final String SETTINGS_DIR = "settings/xml/bitrepository-devel";
    private static final String KEY_FILE = "KeyFile";
    private static final String DEFAULT_CHECKSUM = "0123456789";

    private String DEFAULT_COLLECTION_ID;

    @BeforeMethod(alwaysRun = true)
    public void setupClient() throws Exception {
        DEFAULT_COLLECTION_ID = settingsForTestClient.getCollections().get(0).getID();
    }

    @Test(groups = { "regressiontest" })
    public void defaultSuccessScenarioTest() throws Exception {
        addDescription("Tests simplest arguments for running the CmdLineClient");
        String[] args = new String[]{"-s" + SETTINGS_DIR, 
                "-k" + KEY_FILE,
                "-p" + PILLAR1_ID,
                "-f" + DEFAULT_FILE_ID,
                "-C" + DEFAULT_CHECKSUM,
                "-c" + DEFAULT_COLLECTION_ID};
        new ReplaceFileCmd(args);
    }

    @Test(groups = { "regressiontest" })
    public void URLSuccessScenarioTest() throws Exception {
        addDescription("Tests the scenario, where a URL instead of a file is used for the replacement file.");
        String[] args = new String[]{"-s" + SETTINGS_DIR, 
                "-k" + KEY_FILE,
                "-p" + PILLAR1_ID,
                "-u" + DEFAULT_DOWNLOAD_FILE_ADDRESS,
                "-r" + DEFAULT_CHECKSUM,
                "-C" + DEFAULT_CHECKSUM,
                "-c" + DEFAULT_COLLECTION_ID, 
                "-i" + DEFAULT_FILE_ID};
        new ReplaceFileCmd(args);
    }

    @Test(groups = { "regressiontest" }, expectedExceptions = IllegalArgumentException.class)
    public void missingCollectionArgumentTest() throws Exception {
        addDescription("Tests the scenario, where the collection arguments is missing.");
        String[] args = new String[]{"-s" + SETTINGS_DIR, 
                "-k" + KEY_FILE,
                "-p" + PILLAR1_ID,
                "-u" + DEFAULT_DOWNLOAD_FILE_ADDRESS,
                "-r" + DEFAULT_CHECKSUM,
                "-C" + DEFAULT_CHECKSUM,
                "-i" + DEFAULT_FILE_ID};
        new ReplaceFileCmd(args);
    }

    @Test(groups = { "regressiontest" }, expectedExceptions = IllegalArgumentException.class)
    public void missingPillarArgumentTest() throws Exception {
        addDescription("Tests the different scenarios, with the pillar argument.");
        String[] args = new String[]{"-s" + SETTINGS_DIR, 
                "-k" + KEY_FILE,
                "-u" + DEFAULT_DOWNLOAD_FILE_ADDRESS,
                "-r" + DEFAULT_CHECKSUM,
                "-C" + DEFAULT_CHECKSUM,
                "-c" + DEFAULT_COLLECTION_ID,
                "-i" + DEFAULT_FILE_ID};
        new ReplaceFileCmd(args);
    }

    @Test(groups = { "regressiontest" }, expectedExceptions = IllegalArgumentException.class)
    public void unknownPillarArgumentTest() throws Exception {
        addStep("Testing against a non-existing pillar id", "Should fail");
        String[] args = new String[]{"-s" + SETTINGS_DIR, 
                "-k" + KEY_FILE,
                "-u" + DEFAULT_DOWNLOAD_FILE_ADDRESS,
                "-r" + DEFAULT_CHECKSUM,
                "-C" + DEFAULT_CHECKSUM,
                "-c" + DEFAULT_COLLECTION_ID,
                "-p" + "Random" + (new Date()).getTime() + "pillar",
                "-i" + DEFAULT_FILE_ID};
        new ReplaceFileCmd(args);
    }

    @Test(groups = { "regressiontest" }, expectedExceptions = IllegalArgumentException.class)
    public void missingFileOrURLArgumentTest() throws Exception {
        addDescription("Tests the scenario, where no arguments for file or url is given.");
        String[] args = new String[]{"-s" + SETTINGS_DIR, 
                "-k" + KEY_FILE,
                "-p" + PILLAR1_ID,
                "-r" + DEFAULT_CHECKSUM,
                "-c" + DEFAULT_COLLECTION_ID, 
                "-C" + DEFAULT_CHECKSUM,
                "-i" + DEFAULT_FILE_ID};
        new ReplaceFileCmd(args);
    }
    
    @Test(groups = { "regressiontest" }, expectedExceptions = IllegalArgumentException.class)
    public void bothFileAndURLArgumentTest() throws Exception {
        addDescription("Tests the scenario, where both arguments for file or url is given.");
        String[] args = new String[]{"-s" + SETTINGS_DIR, 
                "-k" + KEY_FILE,
                "-p" + PILLAR1_ID,
                "-r" + DEFAULT_CHECKSUM,
                "-f" + DEFAULT_FILE_ID,
                "-u" + DEFAULT_DOWNLOAD_FILE_ADDRESS,
                "-c" + DEFAULT_COLLECTION_ID, 
                "-C" + DEFAULT_CHECKSUM,
                "-i" + DEFAULT_FILE_ID};
        new ReplaceFileCmd(args);
    }
    
    @Test(groups = { "regressiontest" }, expectedExceptions = IllegalArgumentException.class)
    public void missingFileIDWhenURLArgumentTest() throws Exception {
        addDescription("Tests the scenario, where no checksum argument is given, but a URL is given.");
        String[] args = new String[]{"-s" + SETTINGS_DIR, 
                "-k" + KEY_FILE,
                "-p" + PILLAR1_ID,
                "-u" + DEFAULT_DOWNLOAD_FILE_ADDRESS,
                "-r" + DEFAULT_CHECKSUM,
                "-c" + DEFAULT_COLLECTION_ID, 
                "-C" + DEFAULT_CHECKSUM};
        new ReplaceFileCmd(args);
    }

    @Test(groups = { "regressiontest" })
    public void missingFileIDWhenFileArgumentTest() throws Exception {
        addDescription("Tests the scenario, where no checksum argument is given, but a URL is given.");
        String[] args = new String[]{"-s" + SETTINGS_DIR, 
                "-k" + KEY_FILE,
                "-p" + PILLAR1_ID,
                "-f" + DEFAULT_FILE_ID,
                "-c" + DEFAULT_COLLECTION_ID, 
                "-C" + DEFAULT_CHECKSUM};
        new ReplaceFileCmd(args);
    }

    @Test(groups = { "regressiontest" }, expectedExceptions = IllegalArgumentException.class)
    public void missingChecksumForNewFileWhenUsingURLArgumentTest() throws Exception {
        addDescription("Tests the scenario, where no checksum argument is given, but a URL is given.");
        String[] args = new String[]{"-s" + SETTINGS_DIR, 
                "-k" + KEY_FILE,
                "-p" + PILLAR1_ID,
                "-u" + DEFAULT_DOWNLOAD_FILE_ADDRESS,
                "-C" + DEFAULT_CHECKSUM,
                "-c" + DEFAULT_COLLECTION_ID, 
                "-i" + DEFAULT_FILE_ID};
        new ReplaceFileCmd(args);
    }

    @Test(groups = { "regressiontest" })
    public void missingChecksumForNewFileWhenUsingFileArgumentTest() throws Exception {
        addDescription("Tests the scenario, where no checksum argument is given, but a File is given.");
        String[] args = new String[]{"-s" + SETTINGS_DIR, 
                "-k" + KEY_FILE,
                "-p" + PILLAR1_ID,
                "-f" + DEFAULT_FILE_ID,
                "-C" + DEFAULT_CHECKSUM,
                "-c" + DEFAULT_COLLECTION_ID, 
                "-i" + DEFAULT_FILE_ID};
        new ReplaceFileCmd(args);
    }

    @Test(groups = { "regressiontest" }, expectedExceptions = IllegalArgumentException.class)
    public void missingChecksumForExistingFileWhenUsingURLArgumentTest() throws Exception {
        addDescription("Tests the scenario, where no checksum argument is given, but a URL is given.");
        String[] args = new String[]{"-s" + SETTINGS_DIR, 
                "-k" + KEY_FILE,
                "-p" + PILLAR1_ID,
                "-u" + DEFAULT_DOWNLOAD_FILE_ADDRESS,
                "-r" + DEFAULT_CHECKSUM,
                "-c" + DEFAULT_COLLECTION_ID, 
                "-i" + DEFAULT_FILE_ID};
        new ReplaceFileCmd(args);
    }

    @Test(groups = { "regressiontest" }, expectedExceptions = IllegalArgumentException.class)
    public void missingChecksumForExistingFileWhenUsingFileArgumentTest() throws Exception {
        addDescription("Tests the scenario, where no checksum argument is given, but a File is given.");
        String[] args = new String[]{"-s" + SETTINGS_DIR, 
                "-k" + KEY_FILE,
                "-p" + PILLAR1_ID,
                "-f" + DEFAULT_FILE_ID,
                "-r" + DEFAULT_CHECKSUM,
                "-c" + DEFAULT_COLLECTION_ID, 
                "-i" + DEFAULT_FILE_ID};
        new ReplaceFileCmd(args);
    }
    
    @Test(groups = { "regressiontest" }, expectedExceptions = IllegalArgumentException.class)
    public void badChecksumAlgorithmArgumentTest() throws Exception {
        addDescription("Test failure giving non-existing checksum algorithm as argument.");
        String[] args = new String[]{"-s" + SETTINGS_DIR, 
                "-k" + KEY_FILE,
                "-p" + PILLAR1_ID,
                "-u" + DEFAULT_DOWNLOAD_FILE_ADDRESS,
                "-r" + DEFAULT_CHECKSUM,
                "-C" + DEFAULT_CHECKSUM,
                "-c" + DEFAULT_COLLECTION_ID, 
                "-i" + DEFAULT_FILE_ID,
                "-R" + "NonExistingChecksumType"};
        new ReplaceFileCmd(args);
    }

    @Test(groups = { "regressiontest" })
    public void checksumArgumentNonSaltAlgorithmWitoutSaltTest() throws Exception {
        addDescription("Test MD5 checksum without salt -> no failure");
        String[] args = new String[]{"-s" + SETTINGS_DIR, 
                "-k" + KEY_FILE,
                "-p" + PILLAR1_ID,
                "-u" + DEFAULT_DOWNLOAD_FILE_ADDRESS,
                "-r" + DEFAULT_CHECKSUM,
                "-C" + DEFAULT_CHECKSUM,
                "-c" + DEFAULT_COLLECTION_ID, 
                "-i" + DEFAULT_FILE_ID,
                "-R" + "MD5"};
        new ReplaceFileCmd(args);
    }

    @Test(groups = { "regressiontest" }, expectedExceptions = IllegalArgumentException.class)
    public void checksumArgumentNonSaltAlgorithmWithSaltTest() throws Exception {
        addDescription("Test SHA1 checksum with salt -> failure");
        String[] args = new String[]{"-s" + SETTINGS_DIR, 
                "-k" + KEY_FILE,
                "-p" + PILLAR1_ID,
                "-u" + DEFAULT_DOWNLOAD_FILE_ADDRESS,
                "-r" + DEFAULT_CHECKSUM,
                "-C" + DEFAULT_CHECKSUM,
                "-c" + DEFAULT_COLLECTION_ID, 
                "-i" + DEFAULT_FILE_ID,
                "-R" + "SHA1",
                "-S" + "SALT"};
        new ReplaceFileCmd(args);
    }

    @Test(groups = { "regressiontest" })
    public void checksumArgumentSaltAlgorithmWithSaltTest() throws Exception {
        addDescription("Test HMAC_SHA256 checksum with salt -> No failure");
        String[] args = new String[]{"-s" + SETTINGS_DIR, 
                "-k" + KEY_FILE,
                "-p" + PILLAR1_ID,
                "-u" + DEFAULT_DOWNLOAD_FILE_ADDRESS,
                "-r" + DEFAULT_CHECKSUM,
                "-C" + DEFAULT_CHECKSUM,
                "-c" + DEFAULT_COLLECTION_ID, 
                "-i" + DEFAULT_FILE_ID,
                "-R" + "HMAC_SHA256",
                "-S" + "SALT"};
        new ReplaceFileCmd(args);
    }

    @Test(groups = { "regressiontest" }, expectedExceptions = IllegalArgumentException.class)
    public void checksumArgumentSaltAlgorithmWithoutSaltTest() throws Exception {
        String[] args = new String[]{"-s" + SETTINGS_DIR, 
                "-k" + KEY_FILE,
                "-p" + PILLAR1_ID,
                "-u" + DEFAULT_DOWNLOAD_FILE_ADDRESS,
                "-r" + DEFAULT_CHECKSUM,
                "-C" + DEFAULT_CHECKSUM,
                "-c" + DEFAULT_COLLECTION_ID, 
                "-i" + DEFAULT_FILE_ID,
                "-R" + "HMAC_SHA512"};
        new ReplaceFileCmd(args);
    }
}
