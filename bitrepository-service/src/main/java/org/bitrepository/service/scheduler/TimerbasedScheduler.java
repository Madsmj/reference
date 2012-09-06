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
package org.bitrepository.service.scheduler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Scheduler that uses Timer to run workflows.
 */
public class TimerbasedScheduler implements ServiceScheduler {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());

    /** The timer that schedules events. */
    private final Timer timer;
    /** The period between testing whether triggers have triggered. */
    private final long schedulerInterval;
    /** The map between the running timertasks and their names.*/
    private Map<String, WorkflowTimerTask> intervalTasks = new HashMap<String, WorkflowTimerTask>();
    
    /** The name of the timer.*/
    private static final String TIMER_NAME = "Service Scheduler";
    /** Whether the timer is a deamon.*/
    private static final boolean TIMER_IS_DEAMON = true;
    /** A timer delay of 0 seconds.*/
    private static final Long NO_DELAY = 0L;

    /** Setup a timer task for running the workflows at requested interval.
     *
     * @param interval The interval for the scheduling of a workflow.
     */
    public TimerbasedScheduler(long interval) {
        this.schedulerInterval = interval;
        timer = new Timer(TIMER_NAME, TIMER_IS_DEAMON);
    }

    @Override
    public void scheduleWorkflow(Workflow workflow, String name, Long interval) {
        
        if(cancelWorkflow(name)) {
            log.info("Recreated workflow named '" + name + "': " + workflow);
        } else {
            log.debug("Created a workflow named '" + name + "': " + workflow);
        }
        WorkflowTimerTask task = new WorkflowTimerTask(interval, name, workflow);
        timer.scheduleAtFixedRate(task, NO_DELAY, schedulerInterval);
        intervalTasks.put(name, task);
    }
    
    @Override
    public boolean cancelWorkflow(String name) {
        WorkflowTimerTask task = intervalTasks.remove(name);
        if(task == null) {
            return false;
        }
        
        task.cancel();
        return true;
    }
    
    @Override
    public List<WorkflowTimerTask> getScheduledWorkflows() {
        List<WorkflowTimerTask> workflows = new ArrayList<WorkflowTimerTask>();
        workflows.addAll(intervalTasks.values());
        
        return workflows;
    }
}