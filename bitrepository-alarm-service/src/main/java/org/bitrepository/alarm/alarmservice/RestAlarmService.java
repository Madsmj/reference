/*
 * #%L
 * Bitrepository Alarm Service
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
package org.bitrepository.alarm.alarmservice;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.bitrepository.alarm.AlarmService;
import org.bitrepository.alarm.AlarmServiceFactory;
import org.bitrepository.bitrepositoryelements.Alarm;

@Path("/AlarmService")
public class RestAlarmService {
    private AlarmService alarmService;
    
    public RestAlarmService() {
        alarmService = AlarmServiceFactory.getAlarmService();
    }
    
    /**
     * getShortAlarmList exposes the possibility of getting the list of the most recent alarms  
     * @return A string containing the data of the last alarms. 
     */
    @GET
    @Path("/getShortAlarmList/")
    @Produces("application/json")
    public List<Alarm> getShortAlarmList() {
        List<Alarm> alarmList = new ArrayList<Alarm>();
        alarmList.addAll(alarmService.extractAlarms(null, null, null, null, null, 10, false));
        return alarmList;
    }
    
    /**
     * getFullAlarmList exposes the possibility of getting the list of all alarms received   
     * @return A string containing the data of all alarms received. 
     */
    @GET
    @Path("/getFullAlarmList/")
    @Produces("application/json")
    public List<Alarm> getFullAlarmList() {
        List<Alarm> alarmList = new ArrayList<Alarm>();
        alarmList.addAll(alarmService.extractAlarms(null, null, null, null, null, null, true));
        return alarmList;
    }
}
