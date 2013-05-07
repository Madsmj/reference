/*
 * #%L
 * Bitrepository Audit Trail Service
 * 
 * $Id$
 * $HeadURL$
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
package org.bitrepository.audittrails.collector;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import org.bitrepository.access.getaudittrails.AuditTrailClient;
import org.bitrepository.audittrails.store.AuditTrailStore;
import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.utils.SettingsUtils;
import org.bitrepository.settings.repositorysettings.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages the retrieval of of AuditTrails from contributors.
 */
public class AuditTrailCollector {
    private Logger log = LoggerFactory.getLogger(getClass());
    /** The task for collecting the audits.*/
    private final Map<String, AuditTrailCollectionTimerTask> collectorTasks = new HashMap<String, 
            AuditTrailCollectionTimerTask>();
    /** The timer for keeping track of the collecting task.*/
    private Timer timer;
    private final Settings settings;

    /** Initial grace period in milliseconds after startup to allow the system to finish startup. */
    private static final int DEFAULT_GRACE_PERIOD = 0;

    /**
     * @param settings The settings for this collector.
     * @param client The client for handling the conversation for collecting the audit trails.
     * @param store The storage of the audit trails data.
     */
    public AuditTrailCollector(Settings settings, AuditTrailClient client, AuditTrailStore store) {
        ArgumentValidator.checkNotNull(settings, "settings");
        ArgumentValidator.checkNotNull(client, "AuditTrailClient client");
        ArgumentValidator.checkNotNull(store, "AuditTrailStore store");

        this.settings = settings;
        this.timer = new Timer(true);
        long collectionInterval = settings.getReferenceSettings().getAuditTrailServiceSettings().getTimerTaskCheckInterval();
        
        for(Collection c : settings.getRepositorySettings().getCollections().getCollection()) {
            IncrementalCollector collector = new IncrementalCollector(c.getID(),
                    settings.getReferenceSettings().getAuditTrailServiceSettings().getID(),
                    client, store,
                    settings.getReferenceSettings().getAuditTrailServiceSettings().getMaxNumberOfEventsInRequest());
            AuditTrailCollectionTimerTask collectorTask = new AuditTrailCollectionTimerTask( 
                    collector, settings.getReferenceSettings().getAuditTrailServiceSettings().getCollectAuditInterval());
            log.debug("Will start collection of audit trail every  " + collectionInterval + " ms, " +
                    "after a grace period of " + getGracePeriod() + " ms");
            timer.scheduleAtFixedRate(collectorTask, getGracePeriod(), collectionInterval);
            
            collectorTasks.put(c.getID(), collectorTask);
        }
    }
    
    /**
     * Instantiates a collection of all the newest audit trails.
     */
    public void collectNewestAudits(String collectionID) {
        collectorTasks.get(collectionID).runCollection();
    }

    /**
     * @return The time to wait before starting collection of audit trails. This enables the system to have time to
     * finish startup before they have to start delivering/process audit trails.
     */
    private int getGracePeriod() {
        if (settings.getReferenceSettings().getAuditTrailServiceSettings().isSetGracePeriod()) {
            return settings.getReferenceSettings().getAuditTrailServiceSettings().getGracePeriod().intValue();
        } else {
            return DEFAULT_GRACE_PERIOD;
        }
    }
    
    /**
     * Closes the AuditTrailCollector.
     */
    public void close() {
        for(AuditTrailCollectionTimerTask atctt : collectorTasks.values()) {
            atctt.cancel();
        }
        timer.cancel();
    }

    /**
     * Timer task for keeping track of the automated collecting of audit trails.
     */
    private class AuditTrailCollectionTimerTask extends TimerTask {
        /** The interval between running this timer task.*/
        private final long interval;
        /** The date for the next run.*/
        private Date nextRun;
        private final IncrementalCollector collector;
        
        /**
         * @param interval The interval between running this timer task.
         */
        private AuditTrailCollectionTimerTask(IncrementalCollector collector, long interval) {
            this.collector = collector;
            this.interval = interval;
            nextRun = new Date(System.currentTimeMillis() + getGracePeriod());
            log.debug("Scheduled next collection of audit trails for " + nextRun);
        }
        
        /**
         * Run the operation and when finished set the date for the next collection.
         */
        public synchronized void runCollection() {
            collector.performCollection(SettingsUtils.getAuditContributorsForCollection(settings, 
                    collector.getCollectionID()));
            nextRun = new Date(System.currentTimeMillis() + interval);
        }

        @Override
        public void run() {
            if(nextRun.getTime() < System.currentTimeMillis()) {
                runCollection();
            }
        }
    }
}
