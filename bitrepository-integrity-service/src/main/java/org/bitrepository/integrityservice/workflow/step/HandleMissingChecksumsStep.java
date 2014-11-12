/*
 * #%L
 * Bitrepository Integrity Service
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
package org.bitrepository.integrityservice.workflow.step;

import java.io.IOException;
import java.sql.SQLException;

import org.bitrepository.integrityservice.cache.FileInfo;
import org.bitrepository.integrityservice.cache.IntegrityModel;
import org.bitrepository.integrityservice.cache.database.ChecksumState;
import org.bitrepository.integrityservice.cache.database.FileState;
import org.bitrepository.integrityservice.cache.database.IntegrityIssueIterator;
import org.bitrepository.integrityservice.reports.IntegrityReporter;
import org.bitrepository.service.workflow.AbstractWorkFlowStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A workflow step for finding missing checksums.
 * Uses the IntegrityChecker to perform the actual check.
 */
public class HandleMissingChecksumsStep extends AbstractWorkFlowStep {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());
    /** The Integrity Model. */
    private final IntegrityModel store;
    /** The report model to populate */
    private final IntegrityReporter reporter;
    
    public HandleMissingChecksumsStep(IntegrityModel store, IntegrityReporter reporter) {
        this.store = store;
        this.reporter = reporter;
    }
    
    @Override
    public String getName() {
        return "Handle missing checksums reporting.";
    }

    /**
     * Queries the IntegrityModel for files with missing checksums. Reports them if any is returned.
     * @throws SQLException 
     */
    @Override
    public synchronized void performStep() throws Exception {
        IntegrityIssueIterator missingChecksumsIterator = store.findMissingChecksums(reporter.getCollectionID());
        String file;
        try {
            while((file = missingChecksumsIterator.getNextIntegrityIssue()) != null) {
                for(FileInfo info : store.getFileInfos(file, reporter.getCollectionID())) {
                    if(info.getFileState() == FileState.EXISTING && info.getChecksumState() == ChecksumState.UNKNOWN) {
                        try {
                            reporter.reportMissingChecksum(file, info.getPillarId());
                        } catch (IOException e) {
                            log.error("Failed to report file: " + file + " as having a missing checksum", e);
                        }
                    }
                }
            }
        } finally {
                missingChecksumsIterator.close();
        }
    }

    public static String getDescription() {
        return "Detects and reports files that are missing a checksum from one or more pillars in the collection.";
    }
}
