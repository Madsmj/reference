/*
 * #%L
 * Bitrepository Integrity Client
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
package org.bitrepository.integrityservice.web;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.bitrepository.common.utils.TimeUtils;
import org.bitrepository.integrityservice.IntegrityServiceFactory;
import org.bitrepository.service.workflow.WorkflowTimerTask;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@Path("/IntegrityService")
public class RestIntegrityService {
    private IntegrityServiceWebInterface service;
    
    public RestIntegrityService() {
        service = IntegrityServiceFactory.getIntegrityServiceWebInterface();
    }
    
    @GET
    @Path("/getChecksumErrorFileIDs/")
    @Produces(MediaType.APPLICATION_JSON)
    public String getChecksumErrors(@QueryParam("pillarD") String pillarID) {
        List<String> ids = new ArrayList<String>();
        ids.add("foo");
        ids.add("bar");
        ids.add("baz");
        
        JSONArray array = new JSONArray();
        for(String file : ids) {
            array.put(file);
        }
        return array.toString();
    }
    
    @GET
    @Path("/getMissingFileIDs/")
    @Produces(MediaType.APPLICATION_JSON)
    public String getMissingFileIDs(@QueryParam("pillarD") String pillarID) {
        List<String> ids = new ArrayList<String>();
        ids.add("foo");
        ids.add("bar");
        ids.add("baz");
        
        JSONArray array = new JSONArray();
        for(String file : ids) {
            array.put(file);
        }
        return array.toString();
    }
    
    /**
     * Get the listing of integrity status as a JSON array
     */
    @GET
    @Path("/getIntegrityStatus/")
    @Produces(MediaType.APPLICATION_JSON)
    public String getIntegrityStatus() {
        JSONArray array = new JSONArray();
        List<String> pillars = service.getPillarList();
        for(String pillar : pillars) {
            array.put(makeIntegrityStatusObj(pillar));
        }
        return array.toString();
    }
    
    /***
     * Get the current workflows setup as a JSON array 
     */
    @GET
    @Path("/getWorkflowSetup/")
    @Produces(MediaType.APPLICATION_JSON)
    public String getWorkflowSetup() {
        JSONArray array = new JSONArray();
        Collection<WorkflowTimerTask> workflows = service.getScheduledWorkflows();
        for(WorkflowTimerTask workflow : workflows) {
            array.put(makeWorkflowSetupObj(workflow));
        }
        return array.toString();
    }
    
    /**
     * Get the list of possible workflows as a JSON array 
     */
    @GET
    @Path("/getWorkflowList/")
    @Produces(MediaType.APPLICATION_JSON)
    public String getWorkflowList() {
        JSONArray array = new JSONArray();
        Collection<WorkflowTimerTask> workflows = service.getScheduledWorkflows();
        for(WorkflowTimerTask workflow : workflows) {
            JSONObject obj;       
            try {
                obj = new JSONObject();
                obj.put("workflowID", workflow.getName());
            } catch (JSONException e) {
                obj = (JSONObject) JSONObject.NULL;
            }
            array.put(obj);
        }
        return array.toString();
    }
    
    /**
     * Start a named workflow.  
     */
    @POST
    @Path("/startWorkflow/")
    @Consumes("application/x-www-form-urlencoded")
    @Produces("text/html")
    public String startWorkflow(@FormParam ("workflowID") String workflowID) {
        Collection<WorkflowTimerTask> workflows = service.getScheduledWorkflows();
        for(WorkflowTimerTask workflowTask : workflows) {
            if(workflowTask.getName().equals(workflowID)) {
                workflowTask.runWorkflow();
                return "Workflow '" + workflowID + "' started";        
            }
        }
        return "No workflow named '" + workflowID + "' was found!";
    }
    
    private JSONObject makeIntegrityStatusObj(String pillarID) {
        JSONObject obj = new JSONObject();       
        try {
            obj.put("pillarID", pillarID);
            obj.put("totalFileCount", service.getNumberOfFiles(pillarID));
            obj.put("missingFilesCount",service.getNumberOfMissingFiles(pillarID));
            obj.put("checksumErrorCount", service.getNumberOfChecksumErrors(pillarID));
            return obj;
        } catch (JSONException e) {
            return (JSONObject) JSONObject.NULL;
        }
    }
    
    private JSONObject makeWorkflowSetupObj(WorkflowTimerTask workflowTask) {
        JSONObject obj = new JSONObject();       
        try {
            obj.put("workflowID", workflowTask.getName());
            obj.put("nextRun", workflowTask.getNextRun());
            obj.put("lastRun", workflowTask.getLastRun());
            obj.put("executionInterval", TimeUtils.millisecondsToHuman(workflowTask.getIntervalBetweenRuns()));
            obj.put("currentState", workflowTask.currentState());
            return obj;
        } catch (JSONException e) {
            return (JSONObject) JSONObject.NULL;
        }
    }
    
}
