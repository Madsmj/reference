/*
 * #%L
 * Bitrepository Integrity Client
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
package org.bitrepository.integrityclient;

import java.util.Date;

import org.bitrepository.access.AccessComponentFactory;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumType;
import org.bitrepository.bitrepositoryelements.FileIDs;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.integrityclient.cache.IntegrityCache;
import org.bitrepository.integrityclient.checking.IntegrityChecker;
import org.bitrepository.integrityclient.collector.IntegrityInformationCollector;
import org.bitrepository.integrityclient.scheduler.IntegrityInformationScheduler;
import org.bitrepository.integrityclient.scheduler.triggers.CollectAllChecksumsFromPillarTrigger;
import org.bitrepository.integrityclient.scheduler.triggers.CollectAllFileIDsFromPillarTrigger;
import org.bitrepository.integrityclient.scheduler.triggers.CollectObsoleteChecksumsTrigger;
import org.bitrepository.protocol.ProtocolComponentFactory;

/**
 * Simple integrity service.
 */
public class SimpleIntegrityService {
    /** The default name of the trigger.*/
    private static final String DEFAULT_NAME_OF_CHECKSUM_TRIGGER = "The Checksum Collector Trigger";
    /** The default name of the trigger.*/
    private static final String DEFAULT_NAME_OF_ALL_FILEIDS_TRIGGER = "The FileIDs Collector Trigger For Pillar ";
    /** The default name of the trigger.*/
    private static final String DEFAULT_NAME_OF_ALL_CHECKSUMS_TRIGGER = "The Checksums Collector Trigger For Pillar ";

    /** The scheduler. */
    private final IntegrityInformationScheduler scheduler;
    /** The information collector. */
    private final IntegrityInformationCollector collector;
    /** The cache.*/
    private final IntegrityCache cache;
    /** The integrity checker.*/
    private final IntegrityChecker checker;
    /** The settings. */
    private final Settings settings;
    
    /**
     * Constructor.
     * @param settings The settings for the service.
     */
    public SimpleIntegrityService(Settings settings) {
        this.settings = settings;
        this.cache = IntegrityServiceComponentFactory.getInstance().getCachedIntegrityInformationStorage();
        this.scheduler = IntegrityServiceComponentFactory.getInstance().getIntegrityInformationScheduler(settings);
        this.checker = IntegrityServiceComponentFactory.getInstance().getIntegrityChecker(settings, cache);
        this.collector = IntegrityServiceComponentFactory.getInstance().getIntegrityInformationCollector(
                cache, checker, 
                AccessComponentFactory.getInstance().createGetFileIDsClient(settings),
                AccessComponentFactory.getInstance().createGetChecksumsClient(settings),
                settings,
                ProtocolComponentFactory.getInstance().getMessageBus(settings));
    }
    
    /**
     * Initiates the scheduling of checksum collecting and integrity checking.
     * @param millisSinceLastUpdate The time since last update for a checksum to be calculated.
     * @param intervalBetweenChecks The time between checking for outdated checksums.
     */
    public void startChecksumIntegrityCheck(long millisSinceLastUpdate, long intervalBetweenChecks) {
        // Default checksum used.
        ChecksumSpecTYPE checksumType = new ChecksumSpecTYPE();
        checksumType.setChecksumType(ChecksumType.fromValue(
                settings.getCollectionSettings().getProtocolSettings().getDefaultChecksumType()));
        
        CollectObsoleteChecksumsTrigger trigger = new CollectObsoleteChecksumsTrigger(intervalBetweenChecks, millisSinceLastUpdate, 
                checksumType, collector, cache);
        
        scheduler.putTrigger(DEFAULT_NAME_OF_CHECKSUM_TRIGGER, trigger);
    }
    
    /**
     * Initiates the scheduling of collecting and checking of all the file ids from a single pillar.
     * @param pillarId The id of the pillar to collect file ids from.
     * @param intervalBetweenCollecting The time between collecting all the file ids.
     */
    public void startAllFileIDsIntegrityCheckFromPillar(String pillarId, long intervalBetweenCollecting) {
        CollectAllFileIDsFromPillarTrigger trigger = new CollectAllFileIDsFromPillarTrigger(
                intervalBetweenCollecting, pillarId, collector);
        
        scheduler.putTrigger(DEFAULT_NAME_OF_ALL_FILEIDS_TRIGGER + pillarId, trigger);
    }

    /**
     * Initiates the scheduling of collecting and checking of all the checksums from a single pillar.
     * @param pillarId The id of the pillar to collect file ids from.
     * @param intervalBetweenCollecting The time between collecting all the file ids.
     */
    public void startAllChecksumsIntegrityCheckFromPillar(String pillarId, long intervalBetweenCollecting) {
        ChecksumSpecTYPE checksumType = new ChecksumSpecTYPE();
        checksumType.setChecksumType(ChecksumType.fromValue(
                settings.getCollectionSettings().getProtocolSettings().getDefaultChecksumType()));
        
        CollectAllChecksumsFromPillarTrigger trigger = new CollectAllChecksumsFromPillarTrigger(
                intervalBetweenCollecting, pillarId, checksumType, collector);
        
        scheduler.putTrigger(DEFAULT_NAME_OF_ALL_CHECKSUMS_TRIGGER + pillarId, trigger);
    }
    
    /**
     * Collects and integrity checks the checksum for a given file on all pillars. 
     * Algorithm and salt are optional and can be used for requiring a recalculation of the checksums.
     * @param fileID The id of the file to collect its checksum for.
     * @param checksumAlgorithm The algorithm to use for the checksum collecting. 
     * If null, then the default from settings is used.
     * @param salt The salt for the checksum calculation. If null or empty string, then no salt is used. 
     * @param auditTrailInformation The information for the audit.
     */
    public void checkChecksums(String fileID, String checksumAlgorithm, String salt, String auditTrailInformation) {
        FileIDs fileIDs = new FileIDs();
        fileIDs.setFileID(fileID);
        
        ChecksumSpecTYPE checksumType = new ChecksumSpecTYPE();
        if(checksumAlgorithm == null || checksumAlgorithm.isEmpty()) {
            checksumType.setChecksumType(ChecksumType.fromValue(
                    settings.getCollectionSettings().getProtocolSettings().getDefaultChecksumType()));
        } else {
            checksumType.setChecksumType(ChecksumType.fromValue(checksumAlgorithm));
        }
        
        checksumType.setChecksumSalt(salt.getBytes());
        
        collector.getChecksums(settings.getCollectionSettings().getClientSettings().getPillarIDs(), 
                fileIDs, checksumType, auditTrailInformation);
    }
    
    /**
     * @param pillarId The pillar which has the files.
     * @return The number of files on the given pillar.
     */
    public long getNumberOfFiles(String pillarId) {
        return cache.getNumberOfFiles(pillarId);
    }
    
    /**
     * @param pillarId The pillar which might be missing some files.
     * @return The number of files missing for the given pillar.
     */
    public long getNumberOfMissingFiles(String pillarId) {
        return cache.getNumberOfFiles(pillarId);
    }
    
    /**
     * @param pillarId The pillar which has its file list updated.
     * @return The timestamp for the latest file list update for the given pillar.
     */
    public Date getDateForLastFileUpdate(String pillarId) {
        return cache.getLatestFileUpdate(pillarId);
    }
    
    /**
     * @param pillarId The pillar which might contain files with checksum error.
     * @return The number of files with checksum error at the given pillar.
     */
    public long getNumberOfChecksumErrors(String pillarId) {
        return cache.getNumberOfChecksumErrors(pillarId);
    }
    
    /**
     * @param pillarId The pillar.
     * @return The date for the latest checksum update for the given pillar.
     */
    public Date getDateForLastChecksumUpdate(String pillarId){
        return cache.getLatestChecksumUpdate(pillarId);
    }
}
