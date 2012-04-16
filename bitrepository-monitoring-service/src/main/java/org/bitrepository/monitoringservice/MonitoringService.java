/*
 * #%L
 * Bitrepository Monitoring Service
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
package org.bitrepository.monitoringservice;

import java.util.Map;

import org.bitrepository.access.AccessComponentFactory;
import org.bitrepository.access.getstatus.GetStatusClient;
import org.bitrepository.bitrepositoryelements.ResultingStatus;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.protocol.security.SecurityManager;

public class MonitoringService {

	/** The settings. */
    private final Settings settings;
    /** The security manager */
	private final SecurityManager securityManager;
	/** The store of collected statuses */
	private final ComponentStatusStore statusStore;
	/** The client for getting statuses. */
	private final GetStatusClient getStatusClient;
	/** The status collector */
	private final StatusCollector collector;
    
	public MonitoringService(Settings settings, SecurityManager securityManager) {
		this.settings = settings;
		this.securityManager = securityManager;
		statusStore = new ComponentStatusStore();
		getStatusClient = AccessComponentFactory.getInstance().createGetStatusClient(settings, securityManager);
		collector = new StatusCollector(getStatusClient, settings, statusStore);
		collector.start();
	}
	
	public Map<String, ResultingStatus> getStatus() {
	    return statusStore.getStatusMap();
	}
	
	public void shutdown() {
	    collector.stop();
	    getStatusClient.shutdown();
	}
}