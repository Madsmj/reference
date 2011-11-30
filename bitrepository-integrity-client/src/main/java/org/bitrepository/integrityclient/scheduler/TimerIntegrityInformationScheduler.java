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
package org.bitrepository.integrityclient.scheduler;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.bitrepository.common.settings.Settings;

/**
 * Scheduler that uses Timer to trigger events.
 */
public class TimerIntegrityInformationScheduler implements IntegrityInformationScheduler {
    /** The timer that schedules events. */
    private final Timer timer;
    /** The period between testing whether triggers have triggered. */
    private final long interval;
    /** The map between the running timertasks and their names.*/
    private Map<String, TimerTask> triggerTimerTasks = new HashMap<String, TimerTask>();

    /** Setup a timer task for triggering all triggers at requested interval.
     *
     * @param configuration The configuration for the collection. Currently contains polling interval.
     */
    public TimerIntegrityInformationScheduler(Settings settings) {
        this.interval = settings.getReferenceSettings().getIntegrityServiceSettings().getSchedulerInterval();
        timer = new Timer("Integrity Information Scheduler", true);
    }

    @Override
    public void addTrigger(Trigger trigger, String name) {
        TimerTask task = new TriggerTimerTask(trigger);
        // TODO: Should the interval rather be a suggestion from the trigger?
        // TODO: Should triggers be defined in configuration? How?
        timer.scheduleAtFixedRate(task, 0L, interval);
        
        triggerTimerTasks.put(name, task);
    }
    
    @Override
    public boolean removeTrigger(String name) {
        TimerTask task = triggerTimerTasks.remove(name);
        if(task == null) {
            return false;
        }
        
        task.cancel();
        return true;
    }
    
    private static class TriggerTimerTask extends TimerTask {
        /** The trigger to test and run. */
        private Trigger trigger;

        /** Initialise a task that tests a trigger and runs it if it has triggered.
         *
         * @param trigger The trigger to test and run.
         */
        public TriggerTimerTask(Trigger trigger) {
            this.trigger = trigger;
        }

        @Override
        public void run() {
            if (trigger.isTriggered()) {
                trigger.trigger();
            }
        }
    }
}
